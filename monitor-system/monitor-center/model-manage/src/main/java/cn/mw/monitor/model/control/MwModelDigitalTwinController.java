package cn.mw.monitor.model.control;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.model.service.MwModelDigitalTwinService;
import cn.mw.monitor.service.model.param.MwModelAlertShowParam;
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
 * @date 2023/8/16
 */
@RequestMapping("/mwapi/digitalTwin")
@Controller
@Slf4j
@Api(value = "数字孪生接口", tags = "数字孪生接口")
public class MwModelDigitalTwinController  extends BaseApiService {
    @Autowired
    private MwModelDigitalTwinService mwModelDigitalTwinService;

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getAlertShowInfo/browse")
    @ResponseBody
    @ApiOperation(value = "获取页面展示信息")
    public ResponseBase getAlertShowInfoByRoom(@RequestBody MwModelAlertShowParam param) {
        Reply reply;
        try {
            reply = mwModelDigitalTwinService.getAlertShowInfo(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getRoomInfoByDigitalTwin{}", e);
            return setResultFail("获取页面展示信息失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getPageTypeInfo/browse")
    @ResponseBody
    @ApiOperation(value = "根据Id获取展示页面类型")
    public ResponseBase getPageTypeInfoById(@RequestBody MwModelAlertShowParam param) {
        Reply reply;
        try {
            reply = mwModelDigitalTwinService.getPageTypeInfoById(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getPageTypeInfoById{}", e);
            return setResultFail("根据Id获取展示页面类型失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getLinkInfoById/browse")
    @ResponseBody
    @ApiOperation(value = "根据Id获取链路信息")
    public ResponseBase getLinkInfoById(@RequestBody MwModelAlertShowParam param) {
        Reply reply;
        try {
            reply = mwModelDigitalTwinService.getLinkInfoById(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getLinkInfoById{}", e);
            return setResultFail("根据Id获取链路信息失败", "");
        }
        return setResultSuccess(reply);
    }


}
