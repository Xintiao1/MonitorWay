package cn.mw.monitor.user.control;

import cn.mw.monitor.annotation.MwSysLog;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.api.param.aduser.*;
import cn.mw.monitor.api.param.user.DelteUserParam;
import cn.mw.monitor.api.param.user.QueryUserParam;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.service.common.ServiceException;
import cn.mw.monitor.service.user.exception.UserException;
import cn.mw.monitor.user.service.MWADUserService;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.naming.NamingException;

/**
 * Created by zy.quaee on 2021/4/27 9:18.
 **/
@RequestMapping("/mwapi")
@Controller
@Slf4j
@Api(value = "AD域用户管理接口",tags = "AD域用户管理接口")
public class MWADUserController extends BaseApiService {

    @Autowired
    private MWADUserService mwadUserService;

    /**
     * AD认证
     */
    @ApiOperation(value="AD认证")
    @PostMapping("/ldap/authentic")
    @MwSysLog(value = "AD认证",type = 2)
    @ResponseBody
    public ResponseBase adAuthentic(@RequestBody ADAuthenticParam param) {

        Reply reply ;
        try {
            reply = mwadUserService.authenticAdmin(param);
        } catch (UserException e) {
            log.error(" ad authentic failed 1 2 1 6:",e);
            return setResultFail(ErrorConstant.AD_MSG_100701, e.getMessage());
        }catch (Exception e) {
            return setResultFail(e.getMessage(),param);
        }
        return setResultSuccess(reply);
    }


    /**
     * AD域 OU group
     */
    @ApiOperation(value="获取AD信息")
    @PostMapping("/adServer/browse")
    @MwSysLog(value = "获取AD信息",type = 2)
    @ResponseBody
    public ResponseBase adAuthentic(@RequestBody QueryADInfoParam param) {

        Reply reply ;
        try {
            reply = mwadUserService.select(param);
        } catch (ServiceException | NamingException e) {
            log.error(" ad select OU | group failed 1 2 1 6:",e);
            return setResultFail(ErrorConstant.AD_MSG_100702,e);
        }
        return setResultSuccess(reply);
    }

    /**
     * AD域 用户导入
     */
    @ApiOperation(value="AD用户导入")
    @PostMapping("/adServer/perform")
    @MwSysLog(value = "AD用户导入",type = 2)
    @ResponseBody
    public ResponseBase adUsedPerform(@RequestBody AddADUserParam param) {

        Reply reply = null;
        try {
            reply = mwadUserService.addADUser(param);
        }catch (UserException e) {
            log.error(" add aduser failed 1 2 1 6:",e);
            return setResultFail(ErrorConstant.AD_MSG_100711, e);
        }catch (ServiceException | NamingException e) {
            log.error(" add aduser failed 1 2 1 6:",e);
            return setResultFail(ErrorConstant.AD_MSG_100702, e);
        } catch (Throwable throwable) {
            return setResultFail(ErrorConstant.AD_MSG_100709, param);
        }
        return setResultSuccess(reply);
    }


    /**
     * AD映射配置查询
     */
    @ApiOperation(value="AD映射配置查询")
    @PostMapping("/adServer/configBrowse")
    @MwSysLog(value = "AD映射配置查询",type = 2)
    @ResponseBody
    public ResponseBase configBrowse(@RequestBody QueryADInfoParam param) {

        Reply reply ;
        try {
            reply = mwadUserService.configBrowse(param);
        } catch (Exception e) {
            log.error(" select ad config failed 1 2 1 6:",e);
            return setResultFail(ErrorConstant.AD_MSG_100702,e);
        }
        return setResultSuccess(reply);
    }

    /**
     * AD映射配置删除
     */
    @ApiOperation(value="AD映射配置删除")
    @PostMapping("/adServer/delete")
    @MwSysLog(value = "AD映射配置删除",type = 2)
    @ResponseBody
    public ResponseBase deleteConfig(@RequestBody QueryADInfoParam param) {

        Reply reply ;
        try {
            reply = mwadUserService.deleteById(param);
        } catch (Exception e) {
            log.error("fail to delete ad config :", e);
            return setResultFail("删除配置信息失败!",e);
        }
        return setResultSuccess(reply);
    }

    /**
     * AD域 用户查找
     */
    @ApiOperation(value="AD用户查找")
    @PostMapping("/adServer/userBrowse")
    @MwSysLog(value = "AD用户查找",type = 2)
    @ResponseBody
    public ResponseBase userBrowse(@RequestBody AddADUserParam param) {

        Reply reply ;
        try {
            reply = mwadUserService.selectByName(param);
        } catch (ServiceException  e) {
            log.error(" ad select ad group user failed 1 2 1 6:",e);
            return setResultFail(ErrorConstant.AD_MSG_100702,e);
        }
        return setResultSuccess(reply);
    }

    /**
     * 查看导入AD组用户信息
     */
    @ApiOperation(value="查看导入AD组用户信息")
    @PostMapping("/adServer/groupBrowse")
    @MwSysLog(value = "查看导入AD组用户信息",type = 2)
    @ResponseBody
    public ResponseBase groupUserBrowse(@RequestBody ADGroupUserParam param) {

        Reply reply ;
        try {
            reply = mwadUserService.selectGroupUser(param);
        } catch (ServiceException  e) {
            log.error(" ad select ad group user failed 1 2 1 6:",e);
            return setResultFail(ErrorConstant.AD_MSG_100702,e);
        }
        return setResultSuccess(reply);
    }

    /**
     * SY-AD域服务器信息查询
     */
    @ApiOperation("SY-AD域服务器信息查询")
    @PostMapping("/syAdInfo/browse")
    @MwSysLog(value = "SY-AD域服务器信息查询",type = 2)
    @ResponseBody
    public ResponseBase selectSyAdInfo() {
        try {
            Reply  reply = mwadUserService.seletSyAdInfo();
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("select sy ad info fail ",e);
            return setResultFail("山鹰AD服务器查询失败！",null);
        }
    }

    /**
     * SY-AD域服务器信息保存
     */
    @ApiOperation("SY-AD域服务器信息保存")
    @PostMapping("/syAdInfo/create")
    @MwSysLog(value = "SY-AD域服务器信息保存",type = 2)
    @ResponseBody
    public ResponseBase createSyAdInfo(@RequestBody AdCommonParam param) {
        try {
            Reply  reply = mwadUserService.insertAdInfo(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error(" sy ad info fail to insert ",e);
            return setResultFail("山鹰AD服务器保存失败！",null);
        }
    }

    /**
     * SY-AD映射规则 删除用户
     */
    @ApiOperation("删除用户")
    @PostMapping("/ldapRule/delete")
    @MwSysLog(value = "删除用户",type = 2)
    @ResponseBody
    public ResponseBase deleteUser(@RequestBody DeleteADUserParam param) {
        try {
            Reply  reply = mwadUserService.deleteADUser(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error(" ad rule fail to insert ",e);
            return setResultFail("删除用户失败！",null);
        }
    }

    /**
     * SY-AD映射规则  查询映射规则及规则下用户
     */
    @ApiOperation("查询用户")
    @PostMapping("/ldapRule/browse")
    @MwSysLog(value = "查询用户",type = 2)
    @ResponseBody
    public ResponseBase browseUser(@RequestBody QueryADInfoParam param) {
        try {
            Reply  reply = mwadUserService.browseUser(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error(" ad rule fail to browse ",e);
            return setResultFail("查询用户失败！",null);
        }
    }

    @ApiOperation("同步用户数据")
    @PostMapping("/ldapRule/syncUser")
    @MwSysLog(value = "同步ldap用户", type = 2)
    @ResponseBody
    public ResponseBase syncUser(@RequestBody SyncUserParam param) {
        Reply reply;
        try {
            reply = mwadUserService.syncADUser(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Exception e) {
            log.error(" ad sync user fail to browse ", e);
            return setResultFail("同步用户数据失败！", null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 修改AD配置备注信息
     */
    @ApiOperation(value="修改AD配置备注信息")
    @PostMapping("/adServer/updateDesc")
    @MwSysLog(value = "修改AD配置备注信息",type = 2)
    @ResponseBody
    public ResponseBase updateDesc(@RequestBody AddADUserParam param) {

        Reply reply ;
        try {
            reply = mwadUserService.updateConfigDesc(param);
        } catch (Exception e) {
            log.error("修改AD配置备注信息失败:", e);
            return setResultFail("修改AD配置备注信息失败!",e);
        }
        return setResultSuccess(reply);
    }

    /**
     * 同步AD域机构数据
     */
    @ApiOperation(value="同步AD域机构数据")
    @PostMapping("/adServer/syncOrg")
    @ResponseBody
    public ResponseBase syncOrg(@RequestBody AddADUserParam param) {
        Reply reply ;
        try {
            reply = mwadUserService.syncADOrg();
            if (reply.getRes().equals(PaasConstant.RES_ERROR)) {
                return setResultFail(reply.getMsg(), param);
            }
        } catch (Exception e) {
            log.error("同步AD域机构数据失败:", e);
            return setResultFail("同步AD域机构数据失败!",e);
        }
        return setResultSuccess(reply);
    }
}
