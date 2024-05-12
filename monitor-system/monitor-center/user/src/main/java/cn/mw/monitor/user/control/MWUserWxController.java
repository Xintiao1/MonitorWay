package cn.mw.monitor.user.control;

import cn.mw.monitor.annotation.MwSysLog;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.Constants;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.service.user.dto.LoginDTO;
import cn.mw.monitor.service.user.dto.UserDTO;
import cn.mw.monitor.service.user.exception.ChangePasswdException;
import cn.mw.monitor.service.user.exception.UserException;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.service.user.param.LoginParam;
import cn.mw.monitor.user.common.AuthUtil;
import cn.mw.monitor.user.service.MWPasswordPlanService;
import cn.mw.monitor.user.service.MWUserService;
import cn.mw.monitor.util.RSAUtils;
import cn.mwpaas.common.model.Reply;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.crypto.hash.Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequestMapping("/mwapi")
@RestController
@Slf4j
public class MWUserWxController<value> extends BaseApiService {

    @Autowired
    private MWUserService mwuserService;
    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;

    final HashMap<String,String> maps = new HashMap<>();

    @Value("${weixin.APPID}")
    private String APPID;// = "wxfd9fabe171f626fa";//自己的微信APPID
    @Value("${weixin.APPSECRET}")
    private String APPSECRET; //= "d13e3bed82461d672f1bc4cfdee09d7c";//自己的微信APPSECRET
    @Value("${weixin.CALLBACK}")
    private String callBack; //= "http://auth2wechatq01.monitorway.net/wxAuth/callBack";
    @Value("${weixin.redirectMwUrl}")
    private String redirectMwUrl;

    @ApiOperation(value="微信用户登录")
    @MwSysLog(value = "微信用户登录",type = 1)
    @GetMapping(value = "/loginPage", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody  
    public String loginPage()  {
        //请求获取code的回调地址
        String callBack1 = callBack + "/#/";
        //请求地址
        String url = "https://open.weixin.qq.com/connect/oauth2/authorize" +
                "?appid=" + APPID +
                "&redirect_uri=" + URLEncoder.encode(callBack1) +
                "&response_type=code" +
                "&scope=snsapi_base" +
                "&state=STATE#wechat_redirect";
        return url;
    }

    @PostMapping("/wxAuth/callBack")
    @ResponseBody
    public ResponseBase wxCallBack(@RequestBody Map<String,Object> maps) throws Throwable {
        //获取access_token
        String code = maps.get("code").toString();
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token" +
                "?appid=" + APPID +
                "&secret=" + APPSECRET +
                "&code=" + code +
                "&grant_type=authorization_code";
        JSONObject jsonObject = AuthUtil.doGetJson(url);
        String openid = jsonObject.getString("openid");

        LoginParam loginParam = new LoginParam();
        Reply reply = null;
        Reply reply1 = null;
        try{
            reply1 = mwuserService.selectByOpenid(openid);
            UserDTO userDTO = (UserDTO) reply1.getData();
            if(userDTO == null){
                return setResultWarn(Reply.warn("当前微信账号未绑定用户"));
            }
            String loginName = userDTO.getLoginName();
            //String password = userDTO.getPassword();    //"a1234567";//EncryptsUtil.decrypt(userDTO.getPassword());//"a1234567";//userDTO.getPassword();
            String userId = userDTO.getUserId().toString();
            loginParam.setUserId(userId);
            loginParam.setLoginName(loginName);
            reply = mwuserService.userlogin(loginParam,true);
        }catch (UserException e) {
            return setResult(e.getCode(), e.getMessage(),loginParam);
        } catch (Throwable t) {
            return setResult(Constants.HTTP_RES_CODE_500, t.getMessage(), loginParam);
        }
        LoginDTO loginDTO = (LoginDTO) reply.getData();
        return setResultSuccess(loginDTO);
    }

    @ApiOperation(value="微信用户登录")
    @MwSysLog(value = "微信用户登录",type = 1)
    @GetMapping(value = "/bind/login", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String bindLogin()  {
        //请求获取code的回调地址
        String callBack1 = callBack + "/#/pages/mySelf/mySelf";// "http://developer194.monitorway.net/#/pages/mySelf/mySelf";
        //请求地址
        String url = "https://open.weixin.qq.com/connect/oauth2/authorize" +
                "?appid=" + APPID +
                "&redirect_uri=" + URLEncoder.encode(callBack1) +
                "&response_type=code" +
                "&scope=snsapi_userinfo" +
                "&state=STATE#wechat_redirect";
        return url;
    }


    @PostMapping("/bind/wechat")
    @ResponseBody
    public ResponseBase bindWechat(@RequestBody Map<String,Object> maps) throws Throwable {
        //获取access_token
        String code = maps.get("code").toString();
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token" +
                "?appid=" + APPID +
                "&secret=" + APPSECRET +
                "&code=" + code +
                "&grant_type=authorization_code";
        JSONObject jsonObject = AuthUtil.doGetJson(url);
        String openId = jsonObject.getString("openid");
        Reply reply = null;
        //用户绑定微信
        String loginName = maps.get("loginName").toString();
        Integer userId = iLoginCacheInfo.getCacheInfo(loginName).getUserId();
        if(!userId.toString().isEmpty()){
            //先判断微信是否已经绑定用户了
            reply = mwuserService.selectByOpenid(openId);
            UserDTO userDTO = (UserDTO) reply.getData();
            if(userDTO != null){
                return setResultWarn(Reply.warn("当前微信账号已经绑定用户："+ userDTO.getLoginName()));
            }
            UserDTO userdto = new UserDTO();
            userdto.setUserId(userId);
            userdto.setOpenId(openId);
            try{
                if(openId != null){
                    mwuserService.updateUserOpenId(userdto);
                }else {
                    return setResultWarn(Reply.warn("获取用户openid失败"));
                }
            }catch (UserException e){
                return setResult(e.getCode(), e.getMessage(),loginName);
            }
        }else {
            return setResultWarn(Reply.warn("获取用户id失败"));
        }
        return setResultSuccess("绑定成功");
    }

    @PostMapping(value = "/delBind")
    @ResponseBody
    public ResponseBase delBind(@RequestBody Map<String,Object> maps) throws Throwable {
        String loginName = maps.get("loginName").toString();
        Integer userId = iLoginCacheInfo.getCacheInfo(loginName).getUserId();
        UserDTO userdto = new UserDTO();
        String openId = null;
        userdto.setUserId(userId);
        userdto.setOpenId(openId);
        mwuserService.updateUserOpenId(userdto);
        return setResultSuccess("解除绑定成功");
    }
}
