package cn.mw.module.security.control;

import cn.mw.module.security.dto.EsSysLogRuleDTO;
import cn.mw.module.security.dto.EsSysLogTagDTO;
import cn.mw.module.security.service.EsSysLogRuleService;
import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * 日志规则 页面
 *
 * @author qzg
 * @date 2021/12/08
 */
@RequestMapping("/mwapi/sysLog/rule")
@Controller
@Slf4j
@Api(value = "日志审计-日志规则设置", tags = "日志审计-日志规则设置")
public class EsSysLogRuleController extends BaseApiService {
    @Autowired
    EsSysLogRuleService esSysLogRuleService;


    /**
     * 获取系统日志信息
     *
     * @param param
     * @return
     */
    @MwPermit(moduleName = "log_security")
    @PostMapping("/rulesInfo/browse")
    @ResponseBody
    public ResponseBase getSysLogRulesInfo(@RequestBody EsSysLogRuleDTO param) {
        Reply reply;
        try {
            reply = esSysLogRuleService.getSystemLogRulesInfos(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getSysLogRulesInfo {}",e);
            return setResultFail(e.getMessage(), param);
        }

        return setResultSuccess(reply);
    }


    /**
     * 日志规则信息详情
     *
     * @param param
     * @return
     */
    @MwPermit(moduleName = "log_security")
    @PostMapping("/popup/browse")
    @ResponseBody
    public ResponseBase getRulesInfoById(@RequestBody EsSysLogRuleDTO param) {
        Reply reply;
        try {
            reply = esSysLogRuleService.getRulesInfoById(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getRulesInfoById {}",e);
            return setResultFail(e.getMessage(), param);
        }

        return setResultSuccess(reply);
    }

    /**
     * 新增日志规则
     *
     * @param param
     * @return
     */
    @MwPermit(moduleName = "log_security")
    @PostMapping("/rulesInfo/create")
    @ResponseBody
    public ResponseBase createSysLogRulesInfo(@RequestBody EsSysLogRuleDTO param) {
        Reply reply;
        try {
            reply = esSysLogRuleService.createSysLogRulesInfo(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("createSysLogRulesInfo {}",e);
            return setResultFail(e.getMessage(), param);
        }

        return setResultSuccess(reply);
    }

    /**
     * 修改日志规则
     *
     * @param param
     * @return
     */
    @MwPermit(moduleName = "log_security")
    @PostMapping("/rulesInfo/editor")
    @ResponseBody
    public ResponseBase editoSysLogRulesInfo(@RequestBody EsSysLogRuleDTO param) {
        Reply reply;
        try {
            reply = esSysLogRuleService.editoSysLogRulesInfo(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("editoSysLogRulesInfo {}",e);
            return setResultFail(e.getMessage(), param);
        }

        return setResultSuccess(reply);
    }

    /**
     * 修改日志规则状态
     *
     * @param param
     * @return
     */
    @MwPermit(moduleName = "log_security")
    @PostMapping("/rulesInfo/state")
    @ResponseBody
    public ResponseBase updateSysLogRulesState(@RequestBody EsSysLogRuleDTO param) {
        Reply reply;
        try {
            reply = esSysLogRuleService.updateSysLogRulesState(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("updateSysLogRulesState {}",e);
            return setResultFail(e.getMessage(), param);
        }

        return setResultSuccess(reply);
    }

    /**
     * 删除日志规则
     *
     * @param param
     * @return
     */
    @MwPermit(moduleName = "log_security")
    @PostMapping("/rulesInfo/delete")
    @ResponseBody
    public ResponseBase deleteSysLogRulesInfo(@RequestBody EsSysLogRuleDTO param) {
        Reply reply;
        try {
            reply = esSysLogRuleService.deleteSysLogRulesInfo(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("deleteSysLogRulesInfo {}",e);
            return setResultFail(e.getMessage(), param);
        }

        return setResultSuccess(reply);
    }


    /**
     * 新增标签信息
     *
     * @param param
     * @return
     */
    @MwPermit(moduleName = "log_security")
    @PostMapping("/tagInfo/create")
    @ResponseBody
    public ResponseBase createSysLogTagInfo(@RequestBody EsSysLogTagDTO param) {
        Reply reply;
        try {
            reply = esSysLogRuleService.createTagInfo(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("createSysLogTagInfo {}",e);
            return setResultFail(e.getMessage(), param);
        }

        return setResultSuccess(reply);
    }

    /**
     * 删除标签信息
     *
     * @param param
     * @return
     */
    @MwPermit(moduleName = "log_security")
    @PostMapping("/tagInfo/delete")
    @ResponseBody
    public ResponseBase deleteSysLogTagInfo(@RequestBody EsSysLogTagDTO param) {
        Reply reply;
        try {
            reply = esSysLogRuleService.deleteSysLogTagInfo(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("deleteSysLogTagInfo {}",e);
            return setResultFail(e.getMessage(), param);
        }

        return setResultSuccess(reply);
    }


    /**
     * 查询标签信息
     *
     * @return
     */
    @MwPermit(moduleName = "log_security")
    @PostMapping("/tagInfo/browse")
    @ResponseBody
    public ResponseBase getSysLogTagInfo() {
        Reply reply;
        try {
            reply = esSysLogRuleService.getSysLogTagInfo();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getSysLogTagInfo {}",e);
            return setResultFail(e.getMessage(), "");
        }

        return setResultSuccess(reply);
    }

    /**
     * 模糊匹配显示提示
     *
     * @return
     */
    @MwPermit(moduleName = "log_security")
    @PostMapping("/fuzzSearchFiled")
    @ResponseBody
    public ResponseBase fuzzSearchAllFiledData() {
        Reply reply;
        try {
            reply = esSysLogRuleService.fuzzSearchAllFiledData();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("fuzzSearchAllFiledData {}",e);
            return setResultFail(e.getMessage(), "");
        }

        return setResultSuccess(reply);
    }
}
