package cn.mw.monitor.credential.control;

import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.api.common.SpringUtils;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.common.util.CopyUtils;
import cn.mw.monitor.credential.api.param.AddUpdateCredentialParam;
import cn.mw.monitor.credential.api.param.DeleteCredentialParam;
import cn.mw.monitor.credential.api.param.QueryCredentialParam;
import cn.mw.monitor.credential.model.MwCredentialType;
import cn.mw.monitor.credential.model.MwSnmpCredential;
import cn.mw.monitor.credential.model.MwSnmpPortCredential;
import cn.mw.monitor.credential.service.MwSnmpCredentialService;
import cn.mw.monitor.credential.service.MwSnmpPortCredentialService;
import cn.mw.monitor.credential.service.impl.MwSysCredentialServiceImpl;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.util.MWUtils;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 系统凭据
 *
 * @author zhaoy
 * @since 2021-05-31 14:14:39
 */
@RestController
@RequestMapping("/mwapi")
@Slf4j
@Api(value = "系统凭据管理接口", tags = "系统凭据管理接口")
public class MwSnmpCredentialController extends BaseApiService {
    /**
     * 服务对象
     */
    @Resource
    private MwSnmpCredentialService mwSnmpCredentialService;

    @Resource
    private MwSnmpPortCredentialService mwSnmpPortCredentialService;

    @Autowired
    ILoginCacheInfo loginCacheInfo;

    public boolean isRoleTopId () {
        String loginName = loginCacheInfo.getLoginName();
        String roleId = loginCacheInfo.getRoleId(loginName);
        return MWUtils.ROLE_TOP_ID.equals(roleId);
    }

    /**
     * 新增SNMP凭据
     */
    @ApiModelProperty("新增SNMP凭据")
    @PostMapping("/snmpCredential/create")
    public ResponseBase insertCredential(@RequestBody AddUpdateCredentialParam param) {
        Reply reply;
        try {

            if (MwCredentialType.PORT.name().equals(param.getCredType())) {
                MwSnmpPortCredential msc = copyPortCred(param);
                reply = mwSnmpPortCredentialService.insert(msc);
            } else {
                MwSnmpCredential msc = copyCommNameCred(param);
                reply = mwSnmpCredentialService.insert(msc);
            }

            if (reply.getRes().equals(PaasConstant.RES_ERROR)) {
                return setResultFail(ErrorConstant.USER_MSG_100102, param);
            }
            if (reply.getRes().equals(PaasConstant.RES_WARN)) {
                return setResultWarn(reply);
            }
        } catch (Exception e) {
            return setResultFail(ErrorConstant.CRED_MSG_317001, param);
        }
        return setResultSuccess(param);
    }

    public MwSnmpPortCredential copyPortCred(AddUpdateCredentialParam param) {
        MwSnmpPortCredential msc = CopyUtils.copy(MwSnmpPortCredential.class, param);
        msc.setModule(String.join(",", param.getModules()));
        msc.setModuleId(String.join(",", param.getModuleIds()));
        return msc;
    }

    public MwSnmpCredential copyCommNameCred(AddUpdateCredentialParam param) {
        MwSnmpCredential msc = CopyUtils.copy(MwSnmpCredential.class, param);
        msc.setModule(String.join(",", param.getModules()));
        msc.setModuleId(String.join(",", param.getModuleIds()));
        return msc;
    }
    /**
     * 修改SNMP凭据
     */
    @ApiModelProperty("修改SNMP凭据")
    @PostMapping("/snmpCredential/editor")
    public ResponseBase updateCredential(@RequestBody AddUpdateCredentialParam param) {
        Reply reply;
        try {
            if (MwCredentialType.PORT.name().equals(param.getCredType())) {
                MwSnmpPortCredential msc = copyPortCred(param);
                reply = mwSnmpPortCredentialService.update(msc);
            } else {
                MwSnmpCredential msc = copyCommNameCred(param);
                reply = mwSnmpCredentialService.update(msc);
            }

        } catch (Exception e) {
            return setResultFail(ErrorConstant.CRED_MSG_317001, param);
        }
        return setResultSuccess(param);
    }

    /**
     * 删除SNMP凭据
     */
    @ApiModelProperty("删除SNMP凭据")
    @PostMapping("/snmpCredential/delete")
    public ResponseBase deleteCredential(@RequestBody DeleteCredentialParam param) {
        Reply reply;
        try {
            if (MwCredentialType.PORT.name().equals(param.getCredType())) {
                mwSnmpPortCredentialService.deleteById(param.getIds());
            } else {
                mwSnmpCredentialService.deleteById(param.getIds());
            }

        } catch (Exception e) {
            return setResultFail(ErrorConstant.CRED_MSG_317001, param);
        }
        return setResultSuccess(param);
    }

    /**
     * 查询SNMP凭据列表
     */
    @ApiModelProperty("查询SNMP凭据列表")
    @PostMapping("/snmpCredential/browse")
    public ResponseBase browseCredential(@RequestBody QueryCredentialParam param) {
        Reply reply;
        try {
            boolean flag = isRoleTopId();
            if (MwCredentialType.PORT.name().equals(param.getCredType())) {
                reply = mwSnmpPortCredentialService.pageCredential(param,flag);
            } else {
                reply = mwSnmpCredentialService.pageCredential(param,flag);
            }

        } catch (Exception e) {
            return setResultFail(ErrorConstant.CRED_MSG_317004, param);
        }
        return setResultSuccess(reply);
    }

    /**
     * SNMP凭据下拉框查询
     */
    @ApiModelProperty("SNMP凭据下拉框查询")
    @PostMapping("/snmpCredential/credDropDown")
    public ResponseBase selectCredDropDown(@RequestBody QueryCredentialParam param) {
        Reply reply;
        boolean flag = isRoleTopId();
        try {
            reply = mwSnmpPortCredentialService.selectCredDropDown(param,flag);
        } catch (Exception e) {
            return setResultFail(ErrorConstant.CRED_MSG_317005, "");
        }
        return setResultSuccess(reply);
    }

    /**
     * SNMP凭据查询
     */
    @ApiModelProperty("SNMP凭据查询")
    @PostMapping("/snmpCredential/cred/browse")
    public ResponseBase selectCredById(@RequestBody QueryCredentialParam param) {
        Reply reply;
        try {
            if (MwCredentialType.PORT.name().equals(param.getCredType())) {
                reply = mwSnmpPortCredentialService.selectCredById(param.getCredId());
            } else {
                reply = mwSnmpCredentialService.selectCredById(param.getCredId());
            }
        }catch (Exception e) {
            return setResultFail(ErrorConstant.CRED_MSG_317006,"");
        }
        return setResultSuccess(reply);
    }


}
