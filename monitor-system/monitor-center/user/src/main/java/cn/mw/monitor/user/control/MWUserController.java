package cn.mw.monitor.user.control;

import cn.mw.monitor.annotation.MwLoginLog;
import cn.mw.monitor.annotation.MwSysLog;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.Constants;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.api.common.SpringUtils;
import cn.mw.monitor.api.param.org.QueryOrgForDropDown;
import cn.mw.monitor.api.param.user.*;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.common.util.CopyUtils;
import cn.mw.monitor.service.common.ServiceException;
import cn.mw.monitor.service.user.dto.LoginDTO;
import cn.mw.monitor.service.user.dto.LoginInfo;
import cn.mw.monitor.service.user.dto.UserDTO;
import cn.mw.monitor.service.user.exception.ChangePasswdException;
import cn.mw.monitor.service.user.exception.UserException;
import cn.mw.monitor.service.user.exception.UserLockedException;
import cn.mw.monitor.service.user.param.LoginParam;
import cn.mw.monitor.shiro.KaptchaExtendPlus;
import cn.mw.monitor.user.model.UserOrgGroupDTO;
import cn.mw.monitor.user.service.*;
import cn.mw.monitor.user.service.impl.MWUserLifeCycleManage;
import cn.mw.monitor.util.RSAUtils;
import cn.mw.monitor.validator.group.Insert;
import cn.mw.monitor.validator.group.Update;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.StringUtils;
import com.google.code.kaptcha.servlet.KaptchaExtend;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RequestMapping("/mwapi")
@Controller
@Slf4j
@Api(value = "用户管理接口",tags = "用户管理接口")
public class MWUserController extends BaseApiService {

    @Value("${enableCaptcha}")
    boolean enableCaptcha;

    @Autowired
    private MWUserService mwuserService;

    @Autowired
    private MWOrgService mwOrgService;

    @Autowired
    private MWGroupService mwGroupService;

    @Autowired
    private MwRoleService mwRoleService;

    @Autowired
    private MWPasswordPlanService mwpasswordPlanService;

    @Autowired
    private MWUserLifeCycleManage mwUserLifeCycleManage;

    /**
     * 验证码
     */
    @GetMapping(value = "/login/captcha", produces = MediaType.IMAGE_JPEG_VALUE)
    @ResponseBody
    public void captcha(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        KaptchaExtendPlus kaptchaExtend = (KaptchaExtendPlus) SpringUtils.getBean("kaptchaExtendPlus");
        kaptchaExtend.captcha(request, response);
        String sessionCaptcha = kaptchaExtend.getGeneratedKey(request);
    }

    /**
     * 登录界面logo
     */
    @GetMapping(value = "/login/logo")
    @ResponseBody
    public ResponseBase getLoginLogo(){
        try {
            Reply reply=mwuserService.selectSettingsInfo();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            return setResultFail(e.getMessage(),null);
        }


    }
    /**
     * 用户登录
     */
    @ApiOperation(value="用户登录")
//    @MwSysLog(value = "用户登录",type = 1)
    @MwLoginLog()
    @PostMapping("/user/login")
    @ResponseBody
    @ApiImplicitParams({
            @ApiImplicitParam(name = "loginName",value = "登录名",dataType = "String",paramType = "query",required = true),
            @ApiImplicitParam(name = "password",value = "密码",dataType = "String",paramType = "query",required = true),
            @ApiImplicitParam(name = "captcha",value = "验证码",dataType = "String",paramType = "query",required = true)
    })
    public ResponseBase<Reply<UserDTO>> login(@Validated @RequestBody LoginParam loginParam,
                              HttpServletRequest request, RedirectAttributesModelMap model) {

        KaptchaExtend kaptchaExtend = new KaptchaExtend();
        String sessionCaptcha = kaptchaExtend.getGeneratedKey(request);
        Reply reply = null;
        String loginName = loginParam.getLoginName();
        try {
            //私钥
            String privateKey = RSAUtils.RSA_PRIVATE_KEY;
            //用私钥解密后的用户名和密码
            String loginNameReal = RSAUtils.decryptData(loginParam.getLoginName(),privateKey);
            String passwdReal = RSAUtils.decryptData(loginParam.getPassword(),privateKey);
            if (loginParam.getPasswordCheck()==1){
                passwdReal = loginParam.getPassword();
            }
            log.info("用户登录:{}",loginNameReal+" "+passwdReal);
            loginParam.setLoginName(loginNameReal);
            loginParam.setPassword(passwdReal);
            log.info("用户登录:{}",loginParam);
            Reply reply1 = mwuserService.selectByLoginName(loginParam.getLoginName());
            UserDTO userDTO = (UserDTO) reply1.getData();
            if (userDTO != null) {
                loginParam.setUserId(userDTO.getUserId().toString());
            }
            reply = null;
            if (loginParam.getPasswordCheck()==0){
                reply =  mwuserService.userlogin(loginParam);
            }else {
                if (loginParam.getPassword().equals(userDTO.getPassword())){
                    reply = mwuserService.userlogin(loginParam,true);
                }
            }
            //发布用户登录事件
            mwUserLifeCycleManage.login();
        } catch (ChangePasswdException e) {
            loginParam.setPassword("");
            return setResult(e.getCode(), e.getMessage(), loginParam);
        } catch (UserLockedException e) {
            return setResult(e.getCode(), e.getMessage(),loginParam);
        } catch (UserException e) {
            return setResult(e.getCode(), e.getMessage(),loginParam);
        } catch (Throwable t) {
            return setResult(Constants.HTTP_RES_CODE_500, t.getMessage(), loginParam);
        }

        if(this.enableCaptcha){
            if (null == loginParam.getCaptcha() || !loginParam.getCaptcha().equalsIgnoreCase(sessionCaptcha)) {
                return setResultWarn(Reply.warn("验证码错误"));
            }
        }

        LoginDTO loginDTO = (LoginDTO) reply.getData();
        //不返回密码
        loginParam.setPassword("");
        loginParam.setLoginName(loginName);
        loginDTO.setLoginParam(loginParam);
        return setResultSuccess(loginDTO);
    }

    public static void main(String[] args) {
        String privateKey = RSAUtils.RSA_PRIVATE_KEY;
        //用私钥解密后的用户名和密码
        String loginNameReal = RSAUtils.decryptData("qIVYU2Sd4KIeZCs4kUE2unRHoitiI7PzgdLZJIfDfdLYw2m3BHwRdiu9ZDpb9AlgM2RPYH4wfA4lMTSoclCPfUwgI1fWaCK8OVpJHlXeIVlyc9BhEdGiHtXwnMUFdK7G0JWdLQMzFmFY/5b1Thp0z/6wjLGRqf4QmiYJ83FN0oY=",privateKey);
        System.out.println("loginNameReal"+loginNameReal);
    }

    /**
     * 用户登出
     */
    @ApiOperation(value="用户登出")
    @PostMapping("/user/logout")
    @MwSysLog(value = "用户登出",type = 1)
    @ResponseBody
    public ResponseBase logout(@RequestBody LoginParam loginParam,
                               HttpServletRequest request, RedirectAttributesModelMap model) {

        try {
            mwuserService.userlogout(loginParam.getToken());

        } catch (ServiceException e) {
            return setResultFail(ErrorConstant.USER_MSG_100103, e.getReplyList());
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), loginParam);
        }

        return setResultSuccess(loginParam);
    }
    /**
     * 获取用户数据
     */
    @ApiOperation(value="获取用户数据")
    @PostMapping("/user/loginInfo")
    @ResponseBody
    public ResponseBase loginInfo(@RequestBody LoginParam param,
                                  HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;

        try {
            reply = mwuserService.loginInfo(param.getToken());
        } catch (UserException e) {
            return setResult(e.getCode(), e.getMessage(), param);
        } catch (Throwable t) {
            return setResult(Constants.HTTP_RES_CODE_500, t.getMessage(), param);
        }

        LoginInfo loginInfo = (LoginInfo) reply.getData();
        return setResultSuccess(loginInfo);
    }

    /**
     * 用户解锁
     */
    @ApiOperation(value="用户解锁")
    @PostMapping("/user/unlock")
    @ResponseBody
    public ResponseBase unlock(@RequestBody UnlockParam unlockParam,
                               HttpServletRequest request, RedirectAttributesModelMap model) {

        try {
            UserDTO userDTO = CopyUtils.copy(UserDTO.class, unlockParam);
            Reply reply = mwuserService.unlock(userDTO);
            if (reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), unlockParam);
        }

        return setResultSuccess(unlockParam);
    }

    @ApiOperation(value="用户注册")
    @PostMapping("/user/create")
    @ResponseBody
    @ApiImplicitParams({
            @ApiImplicitParam(name = "loginName",value = "登录名",dataType = "String",paramType = "query",required = true),
            @ApiImplicitParam(name = "userId",value = "用户id",dataType = "Integer",paramType = "query",required = true)
    })
    public ResponseBase register(@Validated({Insert.class}) @RequestBody RegisterParam registerParam,
                                 HttpServletRequest request, RedirectAttributesModelMap model) {

        try {
            UserDTO userDTO = CopyUtils.copy(UserDTO.class, registerParam);
            Reply reply = mwuserService.addUser(userDTO);
            if (reply.getRes() == PaasConstant.RES_ERROR) {
                return setResultFail(ErrorConstant.USER_MSG_100102, registerParam);
            }
            if (reply.getRes() == PaasConstant.RES_WARN) {
                return setResultWarn(reply);
            }
        } catch (ServiceException e) {
            return setResultFail("用户信息填写不完整，"+ErrorConstant.USER_MSG_100102, e.getReplyList());
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), registerParam);
        }

        return setResultSuccess(registerParam);
    }

    /**
     * 用户删除
     */
    @ApiOperation(value="用户删除")
    @PostMapping("/user/delete")
    @ResponseBody
    public ResponseBase deleteUser(@RequestBody DelteUserParam dParam,
                                   HttpServletRequest request, RedirectAttributesModelMap model) {
        try {
            List<Integer> idList = dParam.getUserIdList();
            Reply reply = mwuserService.delete(idList);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), dParam);
        }

        return setResultSuccess(dParam);
    }
    /**
     * 用户状态修改
     */
    @ApiOperation(value="用户状态修改")
    @PostMapping("/user/perform")
    @ResponseBody
    public ResponseBase updateUserStatue(@Validated @RequestBody UpdateUserStateParam updateUserStateParam,
                                         HttpServletRequest request, RedirectAttributesModelMap model) {

        try {
            Reply reply = mwuserService.updateState(updateUserStateParam);
            if (reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), updateUserStateParam);
        }

        return setResultSuccess(updateUserStateParam);
    }
    /**
     * 用户更新
     */
    @ApiOperation(value="用户更新")
    @PostMapping("/user/editor")
    @ResponseBody
    public ResponseBase update(@Validated({Update.class})@RequestBody RegisterParam registerParam,
                               HttpServletRequest request, RedirectAttributesModelMap model) {

        try {
            Reply reply = null;
            UserDTO userDTO = CopyUtils.copy(UserDTO.class, registerParam);
            if (registerParam.getBatchEditor()) {
                reply = mwuserService.batchUpdateUsers(userDTO);
            }else {
                reply = mwuserService.updateUser(userDTO);
            }

            if (!reply.getRes().equals(PaasConstant.RES_SUCCESS)) {
                if (StringUtils.isEmpty(reply.getMsg())) {
                    return setResultFail(ErrorConstant.USER_MSG_100103, reply.getData());
                } else {
                    return setResultFail(reply.getMsg(), reply.getData());
                }
            }
            if (reply.getRes().equals(PaasConstant.RES_WARN)) {
                return setResultWarn(reply);
            }
        } catch (ServiceException e) {
            String msg = "";
            if (e.getReplyList().get(0).getMsg()!=null) {
                msg=e.getReplyList().get(0).getMsg()+",";
            }
            return setResultFail(msg+ErrorConstant.USER_MSG_100103, e.getReplyList());
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), registerParam);
        }

        return setResultSuccess(registerParam);
    }
    /**
     * 用户列表查询
     */
    @ApiOperation(value="用户列表查询")
    @PostMapping(value = "/user/browse")
    @ResponseBody
    public ResponseBase browseUserList(@RequestBody QueryUserParam qParam,
                                       HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {

            reply = mwuserService.pageUser(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), qParam);
        }

        return setResultSuccess(reply);
    }
    /**
     * 用户弹窗查询
     */
    @ApiOperation(value="用户弹窗查询")
    @PostMapping(value = "/user/popup/browse")
    @ResponseBody
    public ResponseBase browsePopupUser(@RequestBody QueryUserParam qParam,
                                        HttpServletRequest request,
                                        RedirectAttributesModelMap model) {
        try {
            Reply reply = mwuserService.selectById(qParam.getUserId());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), qParam);
        }
    }

    /**
     * 用户下拉框查询
     */
    @ApiOperation(value="用户下拉框查询")
    @PostMapping("/user/dropdown/browse")
    @ResponseBody
    public ResponseBase userDropdownBrowse(HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwuserService.getDropDownUser();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 用户,机构,用户组下拉框查询
     */
    @ApiOperation(value="用户,机构,用户组下拉框查询")
    @PostMapping("/userOrgGroup/dropdown/browse")
    @ResponseBody
    public ResponseBase userOrgGroupDropdownBrowse(HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        UserOrgGroupDTO userOrgGroupDTO = new UserOrgGroupDTO();
        try {
            reply = mwuserService.getDropDownUser();
            if (null != reply && reply.getRes() == PaasConstant.RES_SUCCESS) {
                userOrgGroupDTO.setUsers((List)reply.getData());
            }

            QueryOrgForDropDown qParam = new QueryOrgForDropDown();
            reply = mwOrgService.selectDorpdownList(qParam);
            if (null != reply && reply.getRes() == PaasConstant.RES_SUCCESS) {
                userOrgGroupDTO.setOrgs((List)reply.getData());
            }

            reply = mwGroupService.selectDropdown();
            if (null != reply && reply.getRes() == PaasConstant.RES_SUCCESS) {
                userOrgGroupDTO.setGroups((List)reply.getData());
            }

            reply = mwRoleService.selectDorpdownList();
            if (null != reply && reply.getRes() == PaasConstant.RES_SUCCESS) {
                userOrgGroupDTO.setRoles((List)reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), null);
        }
        return setResultSuccess(Reply.ok(userOrgGroupDTO));
    }

    /**
     * 根据权限获取用户信息
     */
    @ApiOperation(value="根据权限获取用户信息")
    @PostMapping("/user/perm/browse")
    @ResponseBody
    public ResponseBase getListByPerm( @RequestBody List<Integer> orgIds,
                                       HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwuserService.selectListByPerm(orgIds);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), null);
        }

        return setResultSuccess(reply);
    }



    /**
     * 修改密码
     */
    @ApiOperation(value="修改密码")
    @PostMapping("/user/password/editor")
    @ResponseBody
    public ResponseBase updatePassword(@Validated({Update.class}) @RequestBody RegisterParam registerParam,
                                       HttpServletRequest request, RedirectAttributesModelMap model) {

        try {
            UserDTO userDTO = CopyUtils.copy(UserDTO.class, registerParam);
            Reply reply = mwuserService.updatePassword(userDTO);
            if (reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(ErrorConstant.USER_MSG_100103, reply.getData());
            }
        } catch (ServiceException e) {
            return setResultFail(e.getReplyList().get(0).getMsg(), e.getReplyList());
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), registerParam);
        }

        return setResultSuccess(registerParam);
    }
    /**
     * 个人设置修改基本信息
     */
    @ApiOperation(value="个人设置修改基本信息")
    @PostMapping("/user/usersetting/editor")
    @ResponseBody
    public ResponseBase updateUserInfo(@Validated({Update.class}) @RequestBody RegisterParam registerParam,
                                       HttpServletRequest request, RedirectAttributesModelMap model) {

        try {
            UserDTO userDTO = CopyUtils.copy(UserDTO.class, registerParam);
            Reply reply = mwuserService.updateUserInfo(userDTO);
            if (reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(ErrorConstant.USER_MSG_100103, reply.getData());
            }
        } catch (ServiceException e) {
            return setResultFail(ErrorConstant.USER_MSG_100103, e.getReplyList());
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), registerParam);
        }

        return setResultSuccess(registerParam);
    }

    /**
     * 初始化自定义列
     */
    @ApiOperation(value="初始化自定义列")
    @PostMapping("/user/customColLoad")
    @ResponseBody
    public ResponseBase customColLoad(@RequestBody UserDTO userDTO,
                                      HttpServletRequest request, RedirectAttributesModelMap model) {

        try {
            Reply reply = mwuserService.customColLoad(userDTO);
            if (reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), userDTO);
        }

        return setResultSuccess(userDTO);
    }
    /**
     * 查询当前用户个人信息
     */
    @ApiOperation(value="查询当前用户个人信息")
    @PostMapping("/user/usersetting/browse")
    @ResponseBody
    public ResponseBase updateUserInfo(@RequestBody UserDTO userDTO) {

        try {
            Reply reply = mwuserService.selectCurrUserInfo(userDTO);
            if (reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(ErrorConstant.USER_MSG_100107, reply.getData());
            }
            return setResultSuccess(reply);
        } catch (ServiceException e) {
            return setResultFail(ErrorConstant.USER_MSG_100107, e.getReplyList());
        }
    }


    /**
     * 重置密码
     */
    @ApiOperation(value="重置密码")
    @PostMapping("/user/password/reset/editor")
    @ResponseBody
    public ResponseBase resetPassword(@Validated({Update.class}) @RequestBody RegisterParam registerParam,
                                       HttpServletRequest request, RedirectAttributesModelMap model) {

        try {
            UserDTO userDTO = CopyUtils.copy(UserDTO.class, registerParam);
            Reply reply = mwuserService.resetPassword(userDTO);
            log.info("重置密码 userDTO:{}",userDTO);
            if (reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(ErrorConstant.USER_MSG_100140, reply.getData());
            }
        } catch (ServiceException e) {
            return setResultFail(e.getReplyList().get(0).getMsg(), e.getReplyList());
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), registerParam);
        }

        return setResultSuccess(registerParam);
    }

    /**
     * 赛尔移动端用户列表查询
     */
    @ApiOperation(value="赛尔移动端用户列表查询")
    @PostMapping(value = "/cernet/user/browse")
    @ResponseBody
    public ResponseBase browseCernetUserList(@RequestBody QueryUserParam qParam,
                                       HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwuserService.getUserList(qParam);
            if (null != reply && !reply.getRes().equals(PaasConstant.RES_SUCCESS)) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), qParam);
        }

        return setResultSuccess(reply);
    }

    /**
     * 获取用户模糊查询的数据
     *
     * @param qParam
     * @param request
     * @param model
     * @return
     */
    @PostMapping("/user/fuzzySearch/browse")
    @ResponseBody
    public ResponseBase fuzzSearchAllFiledData(@RequestBody QueryUserParam qParam,
                                              HttpServletRequest request,
                                              RedirectAttributesModelMap model) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwuserService.getFuzzySearchContent(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), "模糊查询所有字段资数据失败");
        }
        return setResultSuccess(reply);
    }


    /**
     * 下载用户导入模板
     *
     * @param response
     */
    @PostMapping("/user/exportExcelTemplate")
    @ResponseBody
    public void excelTemplateExport(HttpServletResponse response) {
        try {
            mwuserService.excelTemplateExport(response);
        } catch (Exception e) {
            log.error("excelTemplateExport{}", e);
        }
    }

    /**
     * 导出用户数据
     *
     * @param response
     */
    @PostMapping("/user/exportExcel")
    @ApiOperation(value="导出用户数据")
    @ResponseBody
    public void excelUserExport(@RequestBody QueryUserParam qParam,HttpServletResponse response) {
        try {
            mwuserService.exportUserExcel(response,qParam);
        } catch (Exception e) {
            log.error("excelTemplateExport{}", e);
        }
    }

    /**
     * 批量导入资产关联关系
     *
     * @param file     用户导入excel文件
     * @param response 返回数据
     */
    @PostMapping("/user/excelImport")
    @ResponseBody
    public void excelImport(@RequestBody MultipartFile file, HttpServletResponse response) {
        try {
            mwuserService.excelImport(file, response);
        } catch (Exception e) {
            log.error("数据导入失败", e);
        }
    }

    /**
     * 获取所选类别的所有负责人
     *
     * @param param   参数
     * @param request
     * @param model
     * @return
     */
    @PostMapping("/user/getAuthUserList")
    @ResponseBody
    public ResponseBase getAuthUserList(@RequestBody ChangeUserParam param,
                                        HttpServletRequest request,
                                        RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwuserService.getAuthUserList(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("查询负责人数据失败", e);
            return setResultFail(e.getMessage(), "查询负责人数据失败");
        }
        return setResultSuccess(reply);
    }

    /**
     * 更新类别的负责人数据权限
     *
     * @param param   参数
     * @param request
     * @param model
     * @return
     */
    @PostMapping("/user/changeUserAuth")
    @ResponseBody
    public ResponseBase changeUserAuth(@RequestBody ChangeUserParam param,
                                       HttpServletRequest request,
                                       RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwuserService.changeUserAuth(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("更新负责人失败", e);
            return setResultFail(e.getMessage(), "更新负责人失败");
        }
        return setResultSuccess(reply);
    }
}
