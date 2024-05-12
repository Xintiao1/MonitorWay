package cn.mw.module.security.control;

import cn.mw.module.security.dto.EsSysLogAuditQueryDTO;
import cn.mw.module.security.service.EsSysLogAuditService;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * 日志审计 页面
 *
 * @author qzg
 * @date 2021/12/08
 */
@RequestMapping("/mwapi/sysLog/audit")
@Controller
@Slf4j
@Api(value = "日志审计-系统日志查询", tags = "日志审计-系统日志查询")
public class EsSysLogAuditController extends BaseApiService {
    @Autowired
    private EsSysLogAuditService esSysLogAuditService;

    /**
     * 获取系统日志信息
     *
     * @param param
     * @return
     */
    @MwPermit(moduleName = "log_security")
    @PostMapping("/logInfo/browse")
    @ResponseBody
    public ResponseBase getSystemLogInfos(@RequestBody EsSysLogAuditQueryDTO param) {
        Reply reply;
        try {
            reply = esSysLogAuditService.getSystemLogInfos(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getSystemLogInfos {}",e);
            return setResultFail(e.getMessage(), param);
        }

        return setResultSuccess(reply);
    }


    /**
     * 获取系统日志数量
     *
     * @param param
     * @return
     */
    @MwPermit(moduleName = "log_security")
    @PostMapping("/logNum/browse")
    @ResponseBody
    public ResponseBase getSystemLogNums(@RequestBody EsSysLogAuditQueryDTO param) {
        Reply reply;
        try {
            reply = esSysLogAuditService.getSystemLogNums(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getSystemLogNums {}",e);
            return setResultFail(e.getMessage(), param);
        }

        return setResultSuccess(reply);
    }

    /**
     * 获取系统日志数量
     *
     * @param param
     * @return
     */
    @MwPermit(moduleName = "log_security")
    @PostMapping("/getSysLogTree")
    @ResponseBody
    public ResponseBase getSysLogTree(@RequestBody EsSysLogAuditQueryDTO param) {
        Reply reply;
        try {
            reply = esSysLogAuditService.getSysLogTree(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getSysLogTree {}",e);
            return setResultFail(e.getMessage(), param);
        }

        return setResultSuccess(reply);
    }

    /**
     * 模糊查询提示信息
     *
     * @return
     */
    @MwPermit(moduleName = "log_security")
    @PostMapping("/fuzzSearchFiled")
    @ResponseBody
    public ResponseBase fuzzSearchFiled(@RequestBody EsSysLogAuditQueryDTO param) {
        Reply reply;
        try {
            reply = esSysLogAuditService.fuzzSearchFiled(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("fuzzSearchFiled {}",e);
            return setResultFail(e.getMessage(), "");
        }

        return setResultSuccess(reply);
    }


    /**
     * 系统日志导出
     *
     * @return
     */
    @MwPermit(moduleName = "log_security")
    @PostMapping("/sysLogExport")
    @ResponseBody
    public ResponseBase sysLogExport(@RequestBody EsSysLogAuditQueryDTO param, HttpServletRequest request, HttpServletResponse response) {
        Reply reply;
        try {
            reply = esSysLogAuditService.sysLogExport(param, request, response);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("sysLogExport {}",e);
            return setResultFail(e.getMessage(), "");
        }

        return setResultSuccess(reply);
    }


    /**
     * 根据数据源状态初始化连接
     *
     * @return
     */
    @MwPermit(moduleName = "log_security")
    @PostMapping("/initDataSourceState")
    @ResponseBody
    public ResponseBase initDataSourceState() {
        Reply reply;
        try {
            reply = esSysLogAuditService.initDataSourceState();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("initDataSourceState {}",e);
            return setResultFail(e.getMessage(), "");
        }

        return setResultSuccess(reply);
    }
}
