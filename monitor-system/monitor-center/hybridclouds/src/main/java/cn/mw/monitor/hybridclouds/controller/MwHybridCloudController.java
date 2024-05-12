package cn.mw.monitor.hybridclouds.controller;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.hybridclouds.dto.QueryNewHostParam;
import cn.mw.monitor.hybridclouds.service.MwHybridCloudService;
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
 * @date 2021/6/8
 * 我的监控-混合云controller
 */
@RequestMapping("/mwapi/hybridcloud")
@Controller
@Slf4j
@Api(value = "我的监控-混合云接口", tags = "我的监控-混合云接口")
public class MwHybridCloudController extends BaseApiService {
    private static final Logger logger = LoggerFactory.getLogger("MwHybridCloudController");
  @Autowired
  MwHybridCloudService mwHybridCloudService;

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getTreeDate/browse")
    @ResponseBody
    @ApiOperation(value = "查询混合云树结构")
    public ResponseBase getVCenterTree() {
        Reply reply;
        try {
            reply = mwHybridCloudService.getAllTree();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("getVCenterTree {}",e);
            return setResultWarn(e.getMessage());
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getBaseInfo/browse")
    @ResponseBody
    @ApiOperation(value = "查询混合云基本信息数据")
    public ResponseBase getBaseInfo(@RequestBody QueryNewHostParam qParam) {
        Reply reply;
        try {
            reply = mwHybridCloudService.getBasic(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("getBaseInfo {}",e);
            return setResultWarn(e.getMessage());
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getHhyTableData/browse")
    @ResponseBody
    @ApiOperation(value = "查询混合云表格数据")
    public ResponseBase getHhyTableData(@RequestBody QueryNewHostParam qParam) {
        Reply reply;
        try {
            reply = mwHybridCloudService.getHhyTable(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("getHhyTableData {}",e);
            return setResultWarn(e.getMessage());
        }
        return setResultSuccess(reply);
    }

}
