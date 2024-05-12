package cn.mw.monitor.api.controller;

import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.service.systemLog.api.MwSysLogService;
import cn.mw.monitor.service.systemLog.param.EditLogParam;
import cn.mw.monitor.service.systemLog.param.SystemLogParam;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;

@RequestMapping("/mwapi")
@Controller
@Slf4j
@Api(value = "操作日志管理接口", tags = "操作日志管理接口")
public class MWSysLogController extends BaseApiService {

    @Autowired
    private MwSysLogService mwSysLogService;

    @ApiOperation(value = "分页查询日志列表信息")
    @PostMapping("/syslog/browse")
    @ResponseBody
    public ResponseBase browseLog(@RequestBody SystemLogParam qParam,
                                  HttpServletRequest request,
                                  RedirectAttributesModelMap model) {
        try {
            Reply reply = mwSysLogService.selectSysLog(qParam);
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error("日志错误，请联系管理员");
            return setResultFail("日志错误，请联系管理员", null);
        }
    }


    @ApiOperation(value = "查询日志表名")
    @PostMapping("/syslog/browseTableNameKey")
    @ResponseBody
    public ResponseBase browseTableNameKey(HttpServletRequest request,
                                           RedirectAttributesModelMap model,
                                           @PathParam("tableNameType") Integer tableNameType) {
        try {
            Reply reply = mwSysLogService.selectTableName(tableNameType);
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error("日志错误，请联系管理员");
            return setResultFail("日志错误，请联系管理员", null);
        }
    }

    @ApiOperation(value = "表单编辑修改日志保存")
    @PostMapping("/syslog/editor")
    @ResponseBody
    public ResponseBase saveEditLog(@RequestBody EditLogParam param) {
        try {
            mwSysLogService.saveEditLog(param);
            return setResultSuccess();
        } catch (Exception e) {
            return setResultFail("日志错误，请联系管理员", null);
        }
    }

}
