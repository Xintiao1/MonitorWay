package cn.mw.monitor.user.control;

import cn.mw.monitor.annotation.MwSysLog;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.LoadUtil;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.service.user.dto.UserDTO;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.user.common.AuthUtil;
import cn.mw.monitor.user.service.MWUserService;
import cn.mwpaas.common.model.Reply;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@RequestMapping("/mwapi")
@RestController
@Slf4j
public class MWUserQYWxController<value> extends BaseApiService {

    @Autowired
    private MWUserService mwuserService;
    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;

    final HashMap<String,String> maps = new HashMap<>();

    @Value("${qyweixin.ID}")
    private String APPID;// = "wxfd9fabe171f626fa";//企业微信ID
    @Value("${qyweixin.SECRET}")
    private String APPSECRET; //= "d13e3bed82461d672f1bc4cfdee09d7c";//企业微信APPSECRET
    @Value("${qyweixin.AGENTID}")
    private String AGENTID;
    @Value("${qyweixin.CALLBACK}")
    private String callBack;//="http://secdevwechat.monitorway.net/"; //= "http://auth2wechatq01.monitorway.net/wxAuth/callBack";

    @ApiOperation(value="企业微信用户登录")
    @MwSysLog(value = "企业微信用户登录",type = 1)
    @PostMapping(value = "/qyWXLogin", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String qyWXLogin(HttpServletResponse response)  {
        //请求获取code的回调地址
        String callBack1 = callBack + "/mwapi/qywxAuth/callBack";
        //请求地址
        String url = "https://open.weixin.qq.com/connect/oauth2/authorize" +
                "?appid=" + APPID +
                "&redirect_uri=" + URLEncoder.encode(callBack1) +
                "&response_type=code" +
                "&scope=snsapi_base" +
                //"&agentid=" + AGENTID +
                "&state=STATE#wechat_redirect";
        return "data:image/png;base64,"+Base64.encodeBase64String(AuthUtil.encodeQrcode(url,response));
    }

    @GetMapping("/qywxAuth/callBack")
    @ResponseBody
    public ResponseBase qywxCallBack(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        StringBuffer s = request.getRequestURL();
        //response.sendRedirect(s.toString()+"/callBack");
        String code = request.getParameter("code");
        log.info("code:" + code);
        //获取access_token
       /* String url = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?" +
                "corpid=" + APPID +
                "&corpsecret=" + APPSECRET;
        JSONObject jsonObject = AuthUtil.doGetJson(url);
        //JSONObject jsonObject = JSONObject.parseObject(jsonObjecttokenStr);
        String token = jsonObject.getString("access_token");
        String expireIn = jsonObject.getString("expires_in");*/
        String token = mwuserService.getQyWeixinAccessToken(APPID,APPSECRET);

        //获取userId
        String urlUserinfo = "https://qyapi.weixin.qq.com/cgi-bin/user/getuserinfo?" +
                "access_token=" + token +
                "&code=" + code;
        JSONObject json = AuthUtil.doGetJson(urlUserinfo);
        //JSONObject json = JSONObject.parseObject(userInfo);
        String qywxUserId = json.getString("UserId");
        //用户绑定企业微信
        Reply reply = null;
        String loginName = iLoginCacheInfo.getLoginName();;
        Integer userId = iLoginCacheInfo.getCacheInfo(loginName).getUserId();
        if (!userId.toString().isEmpty()) {
            UserDTO userdto = new UserDTO();
            userdto.setUserId(userId);
            userdto.setWechatId(qywxUserId);
            if (qywxUserId != null) {
                mwuserService.updateUserInfo(userdto);
            } else {
                return setResultWarn(Reply.warn("获取用户企业id失败"));
            }
        }
        return setResultSuccess(qywxUserId);
    }


}
