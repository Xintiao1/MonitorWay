package cn.mw.monitor.model.control;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.model.exception.SyncConnectException;
import cn.mw.monitor.model.param.InstanceSyncParam;
import cn.mw.monitor.model.service.MwPrometheusContainerService;
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

/**
 * @author qzg
 * @date 2023/12/25
 */
@RequestMapping("/mwapi/modelContainer")
@Controller
@Slf4j
@Api(value = "资源中心", tags = "监控容器接口")
public class MwPrometheusContainerController extends BaseApiService {

    @Autowired
    private MwPrometheusContainerService mwPrometheusContainerService;

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/containerData/sync")
    @ResponseBody
    @ApiOperation(value = "监控容器同步获取")
    public ResponseBase syncContainerDeviceInfo(@RequestBody InstanceSyncParam param) {
        Reply reply = new Reply();
        try {
            reply = mwPrometheusContainerService.syncContainerDeviceInfo(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        }catch (SyncConnectException e) {
            log.error("syncContainerDeviceInfo{}", e);
            return setResultFail(e.getMessage(), e.getMessage());
        }
        catch (Exception e) {
            log.error("syncContainerDeviceInfo{}", e);
            return setResultFail("监控容器同步获取失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getSelectDrop/browse")
    @ResponseBody
    @ApiOperation(value = "监控容器同步获取")
    public ResponseBase getSelectDropPrometheus(@RequestBody InstanceSyncParam param) {
        Reply reply = new Reply();
        try {
            reply = mwPrometheusContainerService.getSelectDropPrometheus(param.getInstanceId());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        }catch (Exception e) {
            log.error("getSelectDropPrometheus{}", e);
            return setResultFail("监控容器同步获取失败", "");
        }
        return setResultSuccess(reply);
    }

}
