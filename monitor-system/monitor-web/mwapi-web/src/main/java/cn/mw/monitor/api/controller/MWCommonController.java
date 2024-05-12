package cn.mw.monitor.api.controller;

import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.service.assets.api.MwInspectModeService;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.websocket.server.PathParam;

/**
 * @author baochengbin
 * @date 2020/3/17
 */

@RequestMapping("/mwapi")
@Controller
@Slf4j
public class MWCommonController extends BaseApiService {
    private static final Logger logger = LoggerFactory.getLogger("control-" + MWCommonController.class.getName());

    @Value("${model.assets.enable}")
    private Boolean modelAssetEnable;

    @Autowired
    private MwInspectModeService inspectModeService;

    @GetMapping("/common/getAssetsType")
    @ResponseBody
    @ApiOperation(value = "获取当前系统支持资产管理类型")
    public ResponseBase getAssetsType() {
        Reply reply = null;
        try {
            reply = Reply.ok(modelAssetEnable);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return setResultFail(e.getMessage(), "");
        }
        return setResultSuccess(reply);
    }


    /**
     * 开启或关闭检查模式
     * @param enable
     * @return
     */
    @GetMapping("/common/mwInspectMode/openOrClose")
    @ResponseBody
    @ApiOperation(value = "开启或关闭系统检查模式")
    public ResponseBase openOrCloseInspectMode(@PathParam("enable") boolean enable) {
        Reply reply = null;
        try {
            reply = inspectModeService.openOrCloseInspectMode(enable);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return setResultFail(e.getMessage(), "");
        }
        return setResultSuccess(reply);
    }


    /**
     * 获取当前系统是否是检查模式
     * @return
     */
    @GetMapping("/common/getMwInspectMode")
    @ResponseBody
    @ApiOperation(value = "获取检查模式的值")
    public ResponseBase getMwInspectModeInfo() {
        Reply reply = null;
        try {
            reply = Reply.ok(inspectModeService.getInspectModeInfo());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return setResultFail(e.getMessage(), "");
        }
        return setResultSuccess(reply);
    }
}
