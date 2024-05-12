package cn.mw.monitor.model.control;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.model.service.MWModelZabbixMonitorService;
import cn.mw.monitor.service.assets.model.InterfaceRefreshType;
import cn.mw.monitor.service.assets.param.QueryAssetsInterfaceParam;
import cn.mw.monitor.service.assets.param.RefreshInterfaceParam;
import cn.mw.monitor.service.assets.service.MwAssetsInterfaceService;
import cn.mw.monitor.service.model.param.MwModelAssetsInterfaceParam;
import cn.mw.monitor.service.model.service.MWModelZabbixMonitorCommonService;
import cn.mw.monitor.service.model.service.MwModelCommonService;
import cn.mw.monitor.service.server.api.MwServerService;
import cn.mw.monitor.service.server.api.dto.DiskTypeDto;
import cn.mw.monitor.service.server.param.AssetsIdsPageInfoParam;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.servlet.http.HttpServletRequest;

@RequestMapping("/mwapi/modelMonitor")
@Controller
@Slf4j
@Api(value = "资产监控详情查询接口", tags = "资产监控详情查询接口")
public class MWModelZabbixMonitorController extends BaseApiService {

    @Autowired
    private MwAssetsInterfaceService mwAssetsInterfaceService;
    @Autowired
    private MWModelZabbixMonitorCommonService mWModelZabbixMonitorCommonService;
    @Autowired
    private MwModelCommonService mwModelCommonService;
    @Autowired
    private MWModelZabbixMonitorService mwModelZabbixMonitorService;
    @Autowired
    private MwServerService service;

    @Value("${monitor.model.interface}")
    private Boolean isOpen;


    @MwPermit(moduleName = "model_manage")
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
            log.error("getInterfaceIsOpen {}", e);
            return setResultFail("查询接口面板功能启用状态失败", "");
        }
        return setResultSuccess(isFlag);
    }

    /**
     * 更新接口表信息
     */
    @MwPermit(moduleName = "model_manage")
    @PostMapping("/refreshInterfaceInfo/perform")
    @ResponseBody
    public ResponseBase refreshInterfaceInfo(@RequestBody RefreshInterfaceParam refreshInterfaceParam
            , HttpServletRequest request, RedirectAttributesModelMap model) {
        if (StringUtils.isEmpty(refreshInterfaceParam.getRefreshType())) {
            return setResultWarn("未设置更新类型");
        }

        InterfaceRefreshType interfaceRefreshType = null;
        try {
            interfaceRefreshType = InterfaceRefreshType.valueOf(refreshInterfaceParam.getRefreshType());
        } catch (Exception e) {
            return setResultWarn("更新类型类型应是All ,Cust");
        }

        if (interfaceRefreshType == InterfaceRefreshType.Cust
                && (null == refreshInterfaceParam.getAssetIds() || refreshInterfaceParam.getAssetIds().size() == 0)) {
            return setResultWarn("未设置资产id");
        }

        mwAssetsInterfaceService.refreshInterfaceInfo(refreshInterfaceParam);
        return setResultSuccess(Reply.ok());
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getNetDataList/browse")
    @ResponseBody
    @ApiOperation(value = "获得当前hostid的所有网络接口列表的数据")
    public ResponseBase getNetDataList(@RequestBody AssetsIdsPageInfoParam param) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = service.getNetDataList(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("");
            return setResultFail("获取接口列表的数据失败","");
        }

        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
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
            log.error("");
            return setResultFail("接口状态统计失败", "");
        }
        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getNetDetail/browse")
    @ResponseBody
    @ApiOperation(value = "获得当前hostid的网络接口详情")
    public ResponseBase getNetDetail(@RequestBody DiskTypeDto diskTypeDto) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = service.getNetDetail(diskTypeDto);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("");
            return setResultFail("获得网络接口详情", diskTypeDto);
        }

        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/syncAllNetDataList/browse")
    @ResponseBody
    @ApiOperation(value = "同步zabbix网络接口数据")
    public ResponseBase getAllNetDataListByZabbix() {
        Reply reply;
        try {
            // 验证内容正确性
            reply = service.getAllNetDataListByZabbix();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("");
            return setResultFail("同步网络接口数据", e);
        }

        return setResultSuccess(reply);
    }



    @MwPermit(moduleName = "model_manage")
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
            log.error("");
            return setResultFail("获取设备的所有接口信息失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
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
            log.error("");
            return setResultFail("修改接口描述信息失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
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
            log.error("");
            return setResultFail("设置接口告警信息失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
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
            log.error("");
            return setResultFail("修改接口是否展示失败", "");
        }
        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getInterfaceTypeInfo/browse")
    @ResponseBody
    @ApiOperation(value = "获取所有光口电口接口信息")
    public ResponseBase getAllInterface(@RequestBody QueryAssetsInterfaceParam param) {
        Reply reply = null;
        try {
            // 验证内容正确性
            reply = mwAssetsInterfaceService.getAllInterface(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getAllInterface {}", e);
            return setResultFail("获取所有光口电口接口信息失败", "");
        }
        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "model_manage")
    @PostMapping("/queryMonitorServerInfo/browse")
    @ResponseBody
    @ApiOperation(value = "获取监控服务器信息")
    public ResponseBase queryMonitorServerInfo() {
        Reply reply = null;
        try {
            // 验证内容正确性
            reply = mwModelZabbixMonitorService.queryMonitorServerInfo();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("queryMonitorServerInfo {}", e);
            return setResultFail("获取监控服务器信息失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/syncAssetsDetails/create")
    @ResponseBody
    @ApiOperation(value = "同步资产的snmp信息到es数据库中")
    public ResponseBase syncAssetsDetailsToEs() {
        Reply reply = null;
        try {
            // 验证内容正确性
            reply = mwModelZabbixMonitorService.syncAssetsDetailsToEs();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("syncAssetsDetailsToEs {}", e);
            return setResultFail("资产的snmp信息同步失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/updateStatus/editor")
    @ResponseBody
    @ApiOperation(value = "修改接口显示状态")
    public ResponseBase updateInterfaceStatus() {
        Reply reply = null;
        try {
            // 验证内容正确性
            reply = mwModelZabbixMonitorService.updateInterfaceStatus();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("updateInterfaceStatus {}", e);
            return setResultFail("修改接口显示状态失败", "");
        }
        return setResultSuccess(reply);
    }
}
