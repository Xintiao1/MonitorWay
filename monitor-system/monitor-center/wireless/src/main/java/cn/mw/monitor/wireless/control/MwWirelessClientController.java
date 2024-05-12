package cn.mw.monitor.wireless.control;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.wireless.api.param.QueryWirelessAPParam;
import cn.mw.monitor.wireless.dto.QueryWirelessClientParam;
import cn.mw.monitor.wireless.service.MwWirelessAPService;
import cn.mw.monitor.wireless.service.MwWirelessClientService;
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
 * @date 2021/6/16
 */
@RequestMapping("/mwapi/wirelessClient")
@Controller
@Slf4j
@Api(value = "网络设备-无线设备", tags = "网络设备-无线设备接口")
public class MwWirelessClientController extends BaseApiService {
    @Autowired
    private MwWirelessClientService mwWirelessClientService;

    @Autowired
    private MwWirelessAPService mwWirelessAPService;

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("getClientInfo/browse")
    @ResponseBody
    @ApiOperation(value = "查询无线设备客户端列表数据")
    public ResponseBase getClientInfo(@RequestBody QueryWirelessClientParam param) {
        Reply reply;
        try {
            reply = mwWirelessClientService.getClientInfo(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getClientInfo {}",e);
            return setResultWarn(e.getMessage());
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("getAPInfo/browse")
    @ResponseBody
    @ApiOperation(value = "查询无线设备AP端列表数据")
    public ResponseBase getAPInfo(@RequestBody QueryWirelessAPParam param) {
        Reply reply;
        try {
            reply = mwWirelessAPService.getAPTableInfo(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getAPInfo {}",e);
            return setResultWarn(e.getMessage());
        }
        return setResultSuccess(reply);
    }

}
