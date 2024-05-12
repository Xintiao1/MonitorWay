package cn.mw.monitor.api.controller;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.automanage.constant.Constant;
import cn.mw.monitor.automanage.param.AutoManageParam;
import cn.mw.monitor.automanage.service.AutoManageService;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.servlet.http.HttpServletRequest;

/**
 * @author gui.quanwang
 * @className MwAutoManageController
 * @description 自动化运维controller
 * @date 2022/4/5
 */
@RequestMapping("/mwapi")
@Controller
@Api(value = "自动化运维", tags = "自动化运维")
public class MwAutoManageController extends BaseApiService {

    private static final Logger logger = LoggerFactory.getLogger("control-" + MwAutoManageController.class.getName());

    @Autowired
    private AutoManageService autoManageService;

    //    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/autoManage/serverList")
    @ResponseBody
    @ApiOperation(value = "获取服务地址")
    public ResponseBase getServerList(HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = autoManageService.getServerList();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return setResultFail(e.getMessage(), "");
        }
        return setResultSuccess(reply);
    }

    //    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/autoManage/searchInstance")
    @ResponseBody
    @ApiOperation(value = "查询实例对象数据")
    public ResponseBase searchInstance(@RequestParam String serverName,
                                       HttpServletRequest request,
                                       RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            autoManageService.searchServerInstance(Constant.SERVER_NAME);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return setResultFail(e.getMessage(), "");
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/autoManage/instanceList")
    @ResponseBody
    @ApiOperation(value = "获取实例化列表")
    public ResponseBase getInstanceList(@RequestBody AutoManageParam param,
                                        HttpServletRequest request,
                                        RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = autoManageService.getAutoManageList(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return setResultFail(e.getMessage(), "");
        }
        return setResultSuccess(reply);
    }


    @PostMapping("/autoManage/updateInstance")
    @ResponseBody
    @ApiOperation(value = "更新实例化状态")
    public ResponseBase updateInstance(@RequestBody AutoManageParam param,
                                       HttpServletRequest request,
                                       RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = autoManageService.updateServerInstance(param.getId(), param.getServerEnable());
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
