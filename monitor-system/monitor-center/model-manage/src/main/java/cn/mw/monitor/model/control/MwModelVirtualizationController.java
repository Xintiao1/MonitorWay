package cn.mw.monitor.model.control;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.common.util.QueryHostParam;
import cn.mw.monitor.model.param.ModelRelationInstanceUserListParam;
import cn.mw.monitor.model.param.ModelRelationInstanceUserParam;
import cn.mw.monitor.model.param.virtual.ModelVirtualUserListParam;
import cn.mw.monitor.model.param.virtual.ModelVirtualUserParam;
import cn.mw.monitor.model.param.virtual.QueryVirtualInstanceParam;
import cn.mw.monitor.model.service.MwModelVirtualizationService;
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

import javax.servlet.http.HttpServletResponse;

/**
 * @author qzg
 * 模型管理-虚拟化
 * @date 2022/9/7
 */
@RequestMapping("/mwapi/modelVirtual")
@Controller
@Slf4j
@Api(value = "模型管理虚拟化接口", tags = "模型管理虚拟化接口")
public class MwModelVirtualizationController extends BaseApiService {
    @Autowired
    private MwModelVirtualizationService mwModelVirtualizationService;


    @MwPermit(moduleName = "model_manage")
    @PostMapping("virtual/sync")
    @ResponseBody
    @ApiOperation(value = "虚拟化设备同步获取")
    public ResponseBase syncVirtualDeviceInfo(@RequestBody QueryVirtualInstanceParam param) {
        Reply reply = new Reply();
        try {
            reply = mwModelVirtualizationService.syncVirtualDeviceInfo(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("syncVirtualDeviceInfo{}", e);
            return setResultFail("虚拟化设备同步获取失败","");
        }
        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "model_manage")
    @PostMapping("virtual/browseTree")
    @ResponseBody
    @ApiOperation(value = "获取虚拟化树结构数据")
    public ResponseBase getVirtualDeviceTree(@RequestBody QueryVirtualInstanceParam param) {
        Reply reply = new Reply();
        try {
            reply = mwModelVirtualizationService.getVirtualDeviceTree(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getVirtualDeviceTree{}", e);
            return setResultFail("获取虚拟化树结构数据失败","");
        }
        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "model_manage")
    @PostMapping("virtualBase/browse")
    @ResponseBody
    @ApiOperation(value = "获取虚拟化基础数据")
    public ResponseBase getVirtualDeviceBaseInfo(@RequestBody QueryVirtualInstanceParam param) {
        Reply reply = new Reply();
        try {
            reply = mwModelVirtualizationService.getVirtualDeviceBaseInfo(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getVirtualDeviceBaseInfo{}", e);
            return setResultFail("获取虚拟化基础数据失败","");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("virtualList/browse")
    @ResponseBody
    @ApiOperation(value = "获取虚拟化列表数据")
    public ResponseBase getVirtualDeviceInfoList(@RequestBody QueryVirtualInstanceParam param) {
        Reply reply = new Reply();
        try {
            reply = mwModelVirtualizationService.getVirtualDeviceInfoList(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getVirtualDeviceInfoList{}", e);
            return setResultFail("获取虚拟化列表数据失败","");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("virtualHistory/browse")
    @ResponseBody
    @ApiOperation(value = "获取时间段内的虚拟化监测历史数据")
    public ResponseBase getVirtualMonitorInfoByHistory(@RequestBody QueryVirtualInstanceParam param) {
        Reply reply = new Reply();
        try {
            reply = mwModelVirtualizationService.getVirtualMonitorInfoByHistory(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getVirtualMonitorInfoByHistory{}", e);
            return setResultFail("获取监测历史数据失败","");
        }
        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "model_manage")
    @PostMapping("virtualPieSimple/browse")
    @ResponseBody
    @ApiOperation(value = "获取VCenter,DataCenter,cluster的cpu,memory,Datastore饼状图信息")
    public ResponseBase getVirDeviceByPieSimple(@RequestBody QueryVirtualInstanceParam param) {
        Reply reply = new Reply();
        try {
            reply = mwModelVirtualizationService.getVirDeviceByPieSimple(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getVirDeviceByPieSimple{}", e);
            return setResultFail("获取饼状图信息失败","");
        }
        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "model_manage")
    @PostMapping("virtualInfo/export")
    @ResponseBody
    @ApiOperation(value = "虚拟化数据导出")
    public ResponseBase exportVirtualList(@RequestBody QueryVirtualInstanceParam param, HttpServletResponse response) {
        Reply reply = new Reply();
        try {
            reply = mwModelVirtualizationService.exportVirtualList(param, response);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("exportVirtualList{}", e);
            return setResultFail("虚拟化数据导出失败","");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/setVirtualUser/create")
    @ResponseBody
    @ApiOperation(value = "设置虚拟化资产负责人")
    public ResponseBase addVirtualUser(@RequestBody ModelRelationInstanceUserListParam qParam) {
        Reply reply;
        try {
            reply = mwModelVirtualizationService.setVirtualUser(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResult(reply.getRes(), reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("addVirtualUser{}", e);
            return setResultWarn("设置虚拟化资产负责人失败");
        }
        return setResultSuccess(reply.getData());
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/setVirtualUser/browse")
    @ResponseBody
    @ApiOperation(value = "查询虚拟化资产负责人")
    public ResponseBase getVirtualUser(@RequestBody ModelRelationInstanceUserParam param) {
        Reply reply;
        try {
            reply = mwModelVirtualizationService.getVirtualUser(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResult(reply.getRes(), reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getVirtualUser{}", e);
            return setResultWarn("查询虚拟化资产负责人失败");
        }
        return setResultSuccess(reply.getData());
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/queryAssetsInfo/browse")
    @ResponseBody
    @ApiOperation(value = "根据Ip地址查询资产主机id和assetsId")
    public ResponseBase queryAssetsInfo(@RequestBody QueryHostParam qParam) {
        Reply reply;
        try {
            reply = mwModelVirtualizationService.queryAssetsInfo(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResult(reply.getRes(), reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("queryAssetsInfo {}", e);
            return setResultWarn("数据查询失败");
        }
        return setResultSuccess(reply.getData());
    }
}
