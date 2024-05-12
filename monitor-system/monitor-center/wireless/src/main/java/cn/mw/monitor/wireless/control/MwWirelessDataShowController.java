package cn.mw.monitor.wireless.control;

import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.wireless.api.param.QueryWirelessAPParam;
import cn.mw.monitor.wireless.dto.QueryWirelessDataShowParam;
import cn.mw.monitor.wireless.service.MwWirelessAPService;
import cn.mw.monitor.wireless.service.MwWirelessClientService;
import cn.mw.monitor.wireless.service.MwWirelessDataShowService;
import cn.mwpaas.common.constant.PaasConstant;
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
 * @author qzg
 * @date 2021/6/23
 */
@RequestMapping("/mwapi/wirelessData")
@Controller
@Slf4j
@Api(value = "无线设备-数据展示", tags = "无线设备-数据展示接口")
public class MwWirelessDataShowController extends BaseApiService {
    @Autowired
    private MwWirelessDataShowService mwWirelessDataShowService;

    @PostMapping("getUserNumByTime/browse")
    @ResponseBody
    @ApiOperation(value = "查询无线设备人数数据")
    public ResponseBase getUserNumByTime(@RequestBody QueryWirelessDataShowParam param) {
        Reply reply;
        try {
            reply = mwWirelessDataShowService.getUserNumByTime(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getUserNumByTime {}",e);
            return setResultWarn(e.getMessage());
        }
        return setResultSuccess(reply);
    }

    @PostMapping("getFlowByTime/browse")
    @ResponseBody
    @ApiOperation(value = "查询无线设备数据流量")
    public ResponseBase getFlowByTime(@RequestBody QueryWirelessDataShowParam param) {
        Reply reply;
        try {
            reply = mwWirelessDataShowService.getFlowByTime(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getFlowByTime {}",e);
            return setResultWarn(e.getMessage());
        }
        return setResultSuccess(reply);
    }

    @PostMapping("getDataByTXBytes/browse")
    @ResponseBody
    @ApiOperation(value = "查询无线设备发送流量")
    public ResponseBase getDataByTXBytes(@RequestBody QueryWirelessDataShowParam param) {
        Reply reply;
        try {
            reply = mwWirelessDataShowService.getDataByTXBytes(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getDataByTXBytes {}",e);
            return setResultWarn(e.getMessage());
        }
        return setResultSuccess(reply);
    }

    @PostMapping("getDataByRXBytes/browse")
    @ResponseBody
    @ApiOperation(value = "查询无线设备接收流量")
    public ResponseBase getDataByRXBytes(@RequestBody QueryWirelessDataShowParam param) {
        Reply reply;
        try {
            reply = mwWirelessDataShowService.getDataByRXBytes(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getDataByRXBytes {}",e);
            return setResultWarn(e.getMessage());
        }
        return setResultSuccess(reply);
    }

    @PostMapping("getDataByRSSI/browse")
    @ResponseBody
    @ApiOperation(value = "查询无线设备信号强度")
    public ResponseBase getDataByRSSI(@RequestBody QueryWirelessDataShowParam param) {
        Reply reply;
        try {
            reply = mwWirelessDataShowService.getDataByRSSI(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getDataByRXBytes {}",e);
            return setResultWarn(e.getMessage());
        }
        return setResultSuccess(reply);
    }

    @PostMapping("getWirelessDeviceInfo/browse")
    @ResponseBody
    @ApiOperation(value = "查询无线设备基本监控信息")
    public ResponseBase getWirelessDeviceInfo(@RequestBody QueryWirelessDataShowParam param) {
        Reply reply;
        try {
            reply = mwWirelessDataShowService.getWirelessDeviceInfo(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getWirelessDeviceInfo {}",e);
            return setResultWarn(e.getMessage());
        }
        return setResultSuccess(reply);
    }

    @PostMapping("getRSSIDeviceInfo/browse")
    @ResponseBody
    @ApiOperation(value = "查询RSSI设备监控信息")
    public ResponseBase getRSSIDeviceInfo(@RequestBody QueryWirelessDataShowParam param) {
        Reply reply;
        try {
            reply = mwWirelessDataShowService.getRSSIDeviceInfo(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getRSSIDeviceInfo {}",e);
            return setResultWarn(e.getMessage());
        }
        return setResultSuccess(reply);
    }
}
