package cn.mw.monitor.model.control;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.model.param.citrix.MwQueryCitrixParam;
import cn.mw.monitor.model.service.MwModelCitrixService;
import cn.mw.monitor.model.service.MwModelOpenService;
import cn.mw.monitor.service.model.param.QueryDigitalTwinParam;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
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
 * 数字孪生项目开放接口
 * @author qzg
 * @date 2023/8/3
 */
@RequestMapping("/mwapi/ModelDigitalTwin")
@Controller
@Slf4j
@Api(value = "数字孪生开放接口", tags = "数字孪生开放接口")
public class MwModelOpenController  extends BaseApiService {
    @Autowired
    private MwModelOpenService mwModelOpenService;

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getRoomInfo/browse")
    @ResponseBody
    @ApiOperation(value = "获取机房数据")
    public ResponseBase getRoomInfoByDigitalTwin() {
        Reply reply;
        try {
            reply = mwModelOpenService.getRoomInfoByDigitalTwin();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getRoomInfoByDigitalTwin{}", e);
            return setResultFail("获取机房数据失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getLinkInfoById/browse")
    @ResponseBody
    @ApiOperation(value = "获取Id获取链路信息")
    public ResponseBase getLinkInfo() {
        Reply reply;
        try {
            reply = mwModelOpenService.getLinkInfo();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getLinkInfoById{}", e);
            return setResultFail("获取Id获取链路信息失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getAllLinkInfo/browse")
    @ResponseBody
    @ApiOperation(value = "获取所有链路信息")
    public ResponseBase getAllLinkInfo() {
        Reply reply;
        try {
            reply = mwModelOpenService.getAllLinkInfo();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getAllLinkInfo{}", e);
            return setResultFail("获取所有链路信息失败", "");
        }
        return setResultSuccess(reply);
    }
}
