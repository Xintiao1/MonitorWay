package cn.mw.monitor.server.controller;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.server.param.UpdateItemNameParam;
import cn.mw.monitor.server.service.MwItemNameService;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author syt
 * @Date 2021/8/24 15:31
 * @Version 1.0
 */
@RequestMapping("/mwapi/myMonitor")
@Controller
@Api(value = "我的监控，指标详情")
public class MwItemNameController extends BaseApiService {
    private static final Logger logger = LoggerFactory.getLogger("control-MwItemNameController" + MwItemNameController.class.getName());

    @Autowired
    private MwItemNameService itemNameService;

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/itemName/editor")
    @ResponseBody
    @ApiOperation(value = "更新监控项对应中文名称")
    public ResponseBase updateItemChName(@RequestBody UpdateItemNameParam param) {
        Reply reply;
        try {
            reply = itemNameService.updateItemChName(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail("更新监控项对应中文名称失败", param);
        }
        return setResultSuccess(reply);
    }
}
