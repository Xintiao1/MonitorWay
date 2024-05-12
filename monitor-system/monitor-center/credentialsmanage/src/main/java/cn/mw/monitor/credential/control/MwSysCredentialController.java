package cn.mw.monitor.credential.control;

import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.common.util.CopyUtils;
import cn.mw.monitor.credential.api.param.AddUpdateCredentialParam;
import cn.mw.monitor.credential.api.param.DeleteCredentialParam;
import cn.mw.monitor.credential.api.param.MwSysCredParam;
import cn.mw.monitor.credential.api.param.QueryCredentialParam;
import cn.mw.monitor.credential.model.MwSysCredential;
import cn.mw.monitor.credential.service.MwSysCredentialService;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * (MwSysCredential)表控制层
 *
 * @author zhaoy
 * @since 2021-05-31 14:16:08
 */
@RestController
@RequestMapping("/mwapi")
@Slf4j
@Api(value = "系统凭据管理接口",tags = "系统凭据管理接口")
public class MwSysCredentialController extends BaseApiService {
    /**
     * 服务对象
     */
    @Resource
    private MwSysCredentialService mwSysCredentialService;



    /**
     * 新增系统凭据
     */
    @ApiModelProperty("新增系统凭据")
    @PostMapping("/sysCredential/create")
    public ResponseBase insertCredential(@RequestBody AddUpdateCredentialParam param) {
        try {
            MwSysCredential msc = copySysCred(param);
            Reply reply = mwSysCredentialService.insert(msc);
            if (reply.getRes().equals(PaasConstant.RES_ERROR)) {
                return setResultFail(ErrorConstant.USER_MSG_100102, param);
            }
            if (reply.getRes().equals(PaasConstant.RES_WARN)) {
                return setResultWarn(reply);
            }
        }catch (Exception e) {
            return setResultFail(ErrorConstant.CRED_MSG_317001,param);
        }
        return setResultSuccess(param);
    }

    /**
     * 修改系统凭据
     */
    @ApiModelProperty("修改系统凭据")
    @PostMapping("/sysCredential/editor")
    public ResponseBase updateCredential(@RequestBody AddUpdateCredentialParam param) {
        try {
            MwSysCredential msc = copySysCred(param);
            Reply reply = mwSysCredentialService.update(msc);
        }catch (Exception e) {
            return setResultFail(ErrorConstant.CRED_MSG_317001,param);
        }
        return setResultSuccess(param);
    }
    public MwSysCredential copySysCred(AddUpdateCredentialParam param) {
        MwSysCredential msc = CopyUtils.copy(MwSysCredential.class, param);
        msc.setModule(String.join(",", param.getModules()));
        msc.setModuleId(String.join(",", param.getModuleIds()));
        return msc;
    }

    /**
     * 删除系统凭据
     */
    @ApiModelProperty("删除系统凭据")
    @PostMapping("/sysCredential/delete")
    public ResponseBase updateCredential(@RequestBody DeleteCredentialParam param) {
        try {
            Reply reply = mwSysCredentialService.deleteById(param.getIds());
        }catch (Exception e) {
            return setResultFail(ErrorConstant.CRED_MSG_317001,param);
        }
        return setResultSuccess(param);
    }

    /**
     * 查询系统凭据列表
     */
    @ApiModelProperty("查询系统凭据列表")
    @PostMapping("/sysCredential/browse")
    public ResponseBase browseCredential(@RequestBody QueryCredentialParam param) {
        Reply reply;
        try {
            reply = mwSysCredentialService.pageCredential(param);
        }catch (Exception e) {
            return setResultFail(ErrorConstant.CRED_MSG_317004,param);
        }
        return setResultSuccess(reply);
    }

    /**
     * 系统凭据下拉框查询
     */
    @ApiModelProperty("系统凭据下拉框查询")
    @PostMapping("/sysCredential/credDropDown")
    public ResponseBase selectCredDropDown(@RequestBody MwSysCredParam param) {
        Reply reply;
        try {
            reply = mwSysCredentialService.selectCredDropDown(param);
        }catch (Exception e) {
            return setResultFail(ErrorConstant.CRED_MSG_317005,"");
        }
        return setResultSuccess(reply);
    }

    /**
     * 模块下拉框查询
     */
    @ApiModelProperty("模块下拉框查询")
    @PostMapping("/sysCredential/moduleDropDown")
    public ResponseBase selectmoduleDropDown() {
        Reply reply;
        try {
            reply = mwSysCredentialService.getModulesDropDown();
        }catch (Exception e) {
            return setResultFail("查询模块下拉框失败","");
        }
        return setResultSuccess(reply);
    }

    /**
     * 系统凭据查询
     */
    @ApiModelProperty("系统凭据查询")
    @PostMapping("/sysCredential/cred/browse")
    public ResponseBase selectCredById(@RequestBody QueryCredentialParam param) {
        Reply reply;
        try {
            reply = mwSysCredentialService.selectCredById(param.getCredId());
        }catch (Exception e) {
            return setResultFail(ErrorConstant.CRED_MSG_317006,"");
        }
        return setResultSuccess(reply);
    }

}
