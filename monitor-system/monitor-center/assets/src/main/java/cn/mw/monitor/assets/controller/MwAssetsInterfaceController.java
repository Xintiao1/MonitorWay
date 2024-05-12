package cn.mw.monitor.assets.controller;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.service.assets.param.QueryAssetsInterfaceParam;
import cn.mw.monitor.service.assets.service.MwAssetsInterfaceService;
import cn.mw.monitor.service.model.param.MwModelAssetsInterfaceParam;
import cn.mw.monitor.service.model.service.MWModelZabbixMonitorCommonService;
import cn.mw.monitor.service.model.service.MwModelCommonService;
import cn.mw.monitor.service.server.api.MwServerService;
import cn.mw.monitor.service.server.param.AssetsIdsPageInfoParam;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 资产关联zabbix接口信息
 *
 * @author qzg
 * @date 2022/5/11
 */
@RequestMapping("/mwapi/assetsInterface")
@Controller
@Slf4j
@Api(value = "资产关联zabbix接口", tags = "资产关联zabbix接口")
public class MwAssetsInterfaceController extends BaseApiService {
    @Autowired
    private MwAssetsInterfaceService mwAssetsInterfaceService;
    @Autowired
    private MwServerService service;

    @Autowired
    private MWModelZabbixMonitorCommonService mWModelZabbixMonitorCommonService;

    @Autowired
    private MwModelCommonService mwModelCommonService;
    @Value("${monitor.assets.interface}")
    private Boolean isOpen;


    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/getInterface/isOpen")
    @ResponseBody
    @ApiOperation(value = "判断接口面板功能是否启用")
    public ResponseBase getInterfaceIsOpen() {
        boolean isFlag = false;
        try {
            if (isOpen != null) {
                isFlag = isOpen;
            }
        } catch (Throwable e) {
            log.error("getInterfaceIsOpen {}",e);
            return setResultFail("getInterfaceIsOpen() error", "");
        }
        return setResultSuccess(isFlag);
    }

    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/getAllInterface/browse")
    @ResponseBody
    @ApiOperation(value = "获取创建资产时的所有接口")
    public ResponseBase getAllInterface(@RequestBody QueryAssetsInterfaceParam param) {
        Reply reply = null;
        try {
            // 验证内容正确性
            reply = mwAssetsInterfaceService.getAllInterface(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getAllInterface {}",e);
            return setResultFail("getAllInterface() error", "");
        }
        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/setInterfaceStatus/editor")
    @ResponseBody
    @ApiOperation(value = "设置资产接口状态")
    public ResponseBase setInterfaceStatus(@RequestBody QueryAssetsInterfaceParam param) {
        Reply reply = null;
        try {
            // 验证内容正确性
            reply = mwAssetsInterfaceService.setInterfaceStatus(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("setInterfaceStatus {}",e);
            return setResultFail("setInterfaceStatus() error", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/getAllInterface/drop/browse")
    @ResponseBody
    @ApiOperation(value = "获取创建资产时的所有接口（用于流量监控）")
    public ResponseBase getAllDropInterface(@RequestBody QueryAssetsInterfaceParam param) {
        Reply reply = null;
        try {
            // 验证内容正确性
            param.setVlanFlag(false);
            reply = mwAssetsInterfaceService.getAllInterfaces(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getAllDropInterface {}",e);
            return setResultFail("getAllDropInterface() error", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/updateInterfaceDesc/editor")
    @ResponseBody
    @ApiOperation(value = "修改接口描述信息")
    public ResponseBase updateInterfaceDescById(@RequestBody MwModelAssetsInterfaceParam param) {
        Reply reply = null;
        try {
            // 验证内容正确性
            reply = mWModelZabbixMonitorCommonService.updateInterfaceDescById(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("updateInterfaceDescById() error", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/getInterfaceInfo/browse")
    @ResponseBody
    @ApiOperation(value = "获取设备的所有接口信息")
    public ResponseBase getAssetsInterfaceById(@RequestBody AssetsIdsPageInfoParam param) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwModelCommonService.getAllAssetsInterfaceByCriteria(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("getAssetsInterfaceById() error", "");
        }
        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/updateAlertTag/editor")
    @ResponseBody
    @ApiOperation(value = "设置接口告警信息")
    public ResponseBase updateAlertTag(@RequestBody MwModelAssetsInterfaceParam param) {
        Reply reply = null;
        try {
            // 验证内容正确性
            reply = mWModelZabbixMonitorCommonService.updateAlertTag(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("updateAlertTag() error", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/batchUpdateShow/editor")
    @ResponseBody
    @ApiOperation(value = "修改接口是否展示")
    public ResponseBase batchUpdateInterfaceShow(@RequestBody MwModelAssetsInterfaceParam param) {
        Reply reply = null;
        try {
            // 验证内容正确性
            reply = mWModelZabbixMonitorCommonService.batchUpdateInterfaceShow(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("batchUpdateInterfaceShow() error", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/getNetNumCount/editor")
    @ResponseBody
    @ApiOperation(value = "接口状态统计")
    public ResponseBase getNetNumCount(@RequestBody AssetsIdsPageInfoParam param) {
        Reply reply = null;
        try {
            // 验证内容正确性
            reply = service.getNetNumCount(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("getNetNumCount() error", "");
        }
        return setResultSuccess(reply);
    }
}
