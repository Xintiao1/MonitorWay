package cn.mw.monitor.model.control;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.model.param.SystemLogParam;
import cn.mw.monitor.model.service.MwModelSysLogService;
import cn.mwpaas.common.model.Reply;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
 * 模型管理系统 操作日志
 *
 * @author qzg
 * @date 2021/11/11
 */
@RequestMapping("/mwapi/model/sysLog")
@Controller
@Slf4j
@Api(value = "模型管理操作日志", tags = "模型管理操作日志")
public class MwModelSysLogController extends BaseApiService {

    @Autowired
    private MwModelSysLogService mwModelSysLogService;

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/queryChangeHistory")
    @ResponseBody
    @ApiOperation(value = "模型实例变更历史")
    public ResponseBase instaceChangeHistory(@RequestBody SystemLogParam qParam) {
        Reply reply = new Reply();
        try {
            qParam.setType("instance_" + qParam.getInstanceId());
            reply = mwModelSysLogService.getInstaceChangeHistory(qParam);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("instaceChangeHistory{}", e);
            return setResultFail("模型实例变更历史失败", "");
        }
    }

}
