package cn.mw.monitor.netflow.control;

import cn.mw.monitor.agent.param.NetFlowConfigParam;
import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.netflow.entity.NetflowDetailCacheInfo;
import cn.mw.monitor.netflow.param.*;
import cn.mw.monitor.netflow.service.MWNetflowService;
import cn.mw.monitor.netflow.service.OperationType;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RequestMapping("/mwapi")
@Controller
@Slf4j
@Api(value = "流量监控", tags = "流量监控")
public class MWNetflowController extends BaseApiService {

    @Autowired
    private MWNetflowService mwNetflowService;

    /**
     * 添加监控接口
     */
    @ApiOperation(value = "添加监控接口")
    @PostMapping("/netflow/interface/create")
    @ResponseBody
    //@MwPermit(moduleName = "netflow_manage")
    public ResponseBase addInterfaces(@RequestBody NetflowParam netflowParam) {
        Reply reply;
        try {
            reply = mwNetflowService.doInterfaces(netflowParam, OperationType.add);
        } catch (Exception e) {
            log.error("addInterfaces", e);
            return setResultFail(e.getMessage(), netflowParam);
        }
        return setResultSuccess(reply);
    }

    /**
     * 删除监控接口
     */
    @ApiOperation(value = "删除监控接口")
    @PostMapping("/netflow/interface/delete")
    @ResponseBody
    //@MwPermit(moduleName = "netflow_manage")
    public ResponseBase delInterfaces(@RequestBody NetflowParam netflowParam) {
        Reply reply;
        try {
            reply = mwNetflowService.doInterfaces(netflowParam, OperationType.delete);
        } catch (Exception e) {
            log.error("delInterfaces", e);
            return setResultFail(e.getMessage(), netflowParam);
        }
        return setResultSuccess(reply);
    }

    /**
     * 启用,停止监控接口
     */
    @ApiOperation(value = "启用,停止监控接口")
    @PostMapping("/netflow/interface/action")
    @ResponseBody
    //@MwPermit(moduleName = "netflow_manage")
    public ResponseBase performInterfaces(@RequestBody PerformNetflowParam performNetflowParam) {
        Reply reply;
        try {
            if (performNetflowParam.isStart()) {
                reply = mwNetflowService.performInterfaces(performNetflowParam.getParamList(), OperationType.start);
            } else {
                reply = mwNetflowService.performInterfaces(performNetflowParam.getParamList(), OperationType.stop);
            }

        } catch (Exception e) {
            log.error("delInterfaces", e);
            return setResultFail(e.getMessage(), performNetflowParam);
        }
        return setResultSuccess(reply);
    }

    /**
     * 获取流量监控左侧树结构
     */
    @ApiOperation(value = "获取流量监控左侧树结构")
    @PostMapping("/netflow/interface/tree/browse")
    @ResponseBody
    //@MwPermit(moduleName = "netflow_manage")
    public ResponseBase browseTree() {
        Reply reply;
        try {
            reply = mwNetflowService.browseTree();
        } catch (Exception e) {
            log.error("addInterfaces", e);
            return setResultFail(e.getMessage(), null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 获取流量监控结果
     */
    @ApiOperation(value = "获取流量监控结果")
    @PostMapping("/netflow/interface/result/browse")
    @ResponseBody
    //@MwPermit(moduleName = "netflow_manage")
    public ResponseBase browseResult(@RequestBody NetFlowRequestParam requestParam) {
        Reply reply;
        try {
            reply = mwNetflowService.browseResult(requestParam);
        } catch (Exception e) {
            log.error("addInterfaces", e);
            return setResultFail(e.getMessage(), null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 流量管理配置
     */
    @ApiOperation(value = "流量管理配置")
    @PostMapping("/netflow/config/editor")
    @ResponseBody
    //@MwPermit(moduleName = "netflow_manage")
    public ResponseBase netflowConfig(@RequestBody NetFlowConfigParam netFlowConfigParam) {
        Reply reply;
        try {
            reply = mwNetflowService.netflowConfig(netFlowConfigParam);
        } catch (Exception e) {
            log.error("netflowConfig", e);
            return setResultFail(e.getMessage(), netFlowConfigParam);
        }
        return setResultSuccess(reply);
    }

    /**
     * 获取配置信息
     */
    @ApiOperation(value = "获取配置信息")
    @PostMapping("/netflow/config/browse")
    @ResponseBody
    //@MwPermit(moduleName = "netflow_manage")
    public ResponseBase netflowConfigList(@RequestBody NetflowAgentParam netflowAgentParam) {
        Reply reply;
        try {
            reply = mwNetflowService.netflowConfigList(netflowAgentParam);
        } catch (Exception e) {
            log.error("netflowConfigList", e);
            return setResultFail(e.getMessage(), null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 获取IP地址组管理数据
     */
    @ApiOperation(value = "获取IP地址组管理数据")
    @PostMapping("/netflow/ipGroup/browse")
    @ResponseBody
    //@MwPermit(moduleName = "netflow_manage")
    public ResponseBase browseIpGroup(@RequestBody IpGroupRequestParam requestParam) {
        Reply reply;
        try {
            reply = mwNetflowService.browseIpGroup(requestParam);
        } catch (Exception e) {
            log.error("获取IP地址组管理数据失败", e);
            return setResultFail(e.getMessage(), null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 获取IP地址组管理数据
     */
    @ApiOperation(value = "获取IP地址组管理下拉数据")
    @PostMapping("/netflow/ipGroup/drop/browse")
    @ResponseBody
    //@MwPermit(moduleName = "netflow_manage")
    public ResponseBase dropBrowseIpGroup(@RequestBody IpGroupRequestParam requestParam) {
        Reply reply;
        try {
            reply = mwNetflowService.dropBrowseIpGroup(requestParam);
        } catch (Exception e) {
            log.error("获取IP地址组管理数据失败", e);
            return setResultFail(e.getMessage(), null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 获取IP地址组管理数据
     */
    @ApiOperation(value = "获取单个IP地址组数据")
    @PostMapping("/netflow/ipGroup/browseOne")
    @ResponseBody
    //@MwPermit(moduleName = "netflow_manage")
    public ResponseBase browseOneIpGroup(@RequestBody IpGroupRequestParam requestParam) {
        Reply reply;
        try {
            reply = mwNetflowService.browseOneIpGroup(requestParam);
        } catch (Exception e) {
            log.error("获取IP地址组管理数据失败", e);
            return setResultFail(e.getMessage(), null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 添加IP地址组数据
     */
    @ApiOperation(value = "添加IP地址组数据")
    @PostMapping("/netflow/ipGroup/create")
    @ResponseBody
    //@MwPermit(moduleName = "netflow_manage")
    public ResponseBase addIpGroup(@RequestBody IpGroupRequestParam requestParam) {
        Reply reply;
        try {
            reply = mwNetflowService.addIpGroup(requestParam);
        } catch (Exception e) {
            log.error("添加IP地址组数据失败", e);
            return setResultFail(e.getMessage(), null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 编辑IP地址组数据
     */
    @ApiOperation(value = "编辑IP地址组数据")
    @PostMapping("/netflow/ipGroup/edit")
    @ResponseBody
    //@MwPermit(moduleName = "netflow_manage")
    public ResponseBase editIpGroup(@RequestBody IpGroupRequestParam requestParam) {
        Reply reply;
        try {
            reply = mwNetflowService.editIpGroup(requestParam);
        } catch (Exception e) {
            log.error("编辑IP地址组数据失败", e);
            return setResultFail(e.getMessage(), null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 删除IP地址组数据
     */
    @ApiOperation(value = "删除IP地址组数据")
    @PostMapping("/netflow/ipGroup/delete")
    @ResponseBody
    //@MwPermit(moduleName = "netflow_manage")
    public ResponseBase deleteIpGroup(@RequestBody IpGroupRequestParam requestParam) {
        Reply reply;
        try {
            reply = mwNetflowService.deleteIpGroup(requestParam);
        } catch (Exception e) {
            log.error("删除IP地址组数据失败", e);
            return setResultFail(e.getMessage(), null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 删除IP地址组数据
     */
    @ApiOperation(value = "更新IP地址组数据状态")
    @PostMapping("/netflow/ipGroup/updateState")
    @ResponseBody
    //@MwPermit(moduleName = "netflow_manage")
    public ResponseBase updateState(@RequestBody IpGroupRequestParam requestParam) {
        Reply reply;
        try {
            reply = mwNetflowService.updateIpGroupState(requestParam);
        } catch (Exception e) {
            log.error("更新IP地址组数据状态失败", e);
            return setResultFail(e.getMessage(), null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 添加应用数据
     */
    @ApiOperation(value = "添加应用数据")
    @PostMapping("/netflow/application/create")
    @ResponseBody
    //@MwPermit(moduleName = "netflow_manage")
    public ResponseBase addApp(@RequestBody ApplicationRequestParam requestParam) {
        Reply reply;
        try {
            reply = mwNetflowService.addApp(requestParam);
        } catch (Exception e) {
            log.error("添加应用数据失败", e);
            return setResultFail(e.getMessage(), null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 查看应用数据
     */
    @ApiOperation(value = "查看应用数据")
    @PostMapping("/netflow/application/browse")
    @ResponseBody
    //@MwPermit(moduleName = "netflow_manage")
    public ResponseBase browseApp(@RequestBody ApplicationRequestParam requestParam) {
        Reply reply;
        try {
            reply = mwNetflowService.browseApp(requestParam);
        } catch (Exception e) {
            log.error("查看应用数据失败", e);
            return setResultFail(e.getMessage(), null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 查看单个应用数据
     */
    @ApiOperation(value = "查看单个应用数据")
    @PostMapping("/netflow/application/browseOne")
    @ResponseBody
    //@MwPermit(moduleName = "netflow_manage")
    public ResponseBase browseOneApp(@RequestBody ApplicationRequestParam requestParam) {
        Reply reply;
        try {
            reply = mwNetflowService.browseOneApp(requestParam);
        } catch (Exception e) {
            log.error("查看单个应用数据失败", e);
            return setResultFail(e.getMessage(), null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 编辑应用数据
     */
    @ApiOperation(value = "编辑应用数据")
    @PostMapping("/netflow/application/edit")
    @ResponseBody
    //@MwPermit(moduleName = "netflow_manage")
    public ResponseBase editApp(@RequestBody ApplicationRequestParam requestParam) {
        Reply reply;
        try {
            reply = mwNetflowService.editApp(requestParam);
        } catch (Exception e) {
            log.error("编辑应用数据失败", e);
            return setResultFail(e.getMessage(), null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 删除应用数据
     */
    @ApiOperation(value = "删除应用数据")
    @PostMapping("/netflow/application/delete")
    @ResponseBody
    //@MwPermit(moduleName = "netflow_manage")
    public ResponseBase deleteApp(@RequestBody ApplicationRequestParam requestParam) {
        Reply reply;
        try {
            reply = mwNetflowService.deleteApp(requestParam);
        } catch (Exception e) {
            log.error("删除应用数据失败", e);
            return setResultFail(e.getMessage(), null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 更新应用数据状态
     */
    @ApiOperation(value = "更新应用数据状态")
    @PostMapping("/netflow/application/updateState")
    @ResponseBody
    //@MwPermit(moduleName = "netflow_manage")
    public ResponseBase updateState(@RequestBody ApplicationRequestParam requestParam) {
        Reply reply;
        try {
            reply = mwNetflowService.updateAppState(requestParam);
        } catch (Exception e) {
            log.error("更新应用数据状态失败", e);
            return setResultFail(e.getMessage(), null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 编辑监控接口
     */
    @ApiOperation(value = "编辑监控接口")
    @PostMapping("/netflow/interface/editor")
    @ResponseBody
    //@MwPermit(moduleName = "netflow_manage")
    public ResponseBase editorInterfaces(@RequestBody AssetParam assetParam) {
        Reply reply;
        try {
            reply = mwNetflowService.editorInterfaces(assetParam);
        } catch (Exception e) {
            log.error("editorInterfaces", e);
            return setResultFail(e.getMessage(), assetParam);
        }
        return setResultSuccess(reply);
    }

    /**
     * 编辑前获取监控接口数据
     */
    @ApiOperation(value = "编辑前获取监控接口数据")
    @PostMapping("/netflow/interface/popup/browse")
    @ResponseBody
    //@MwPermit(moduleName = "netflow_manage")
    public ResponseBase popupInterfaces(@RequestBody AssetParam assetParam) {
        Reply reply;
        try {
            reply = mwNetflowService.popupInterfaces(assetParam);
        } catch (Exception e) {
            log.error("popupInterfaces", e);
            return setResultFail(e.getMessage(), assetParam);
        }
        return setResultSuccess(reply);
    }

    /**
     * 编辑前获取监控接口数据
     */
    @ApiOperation(value = "流量明细获取数据")
    @PostMapping("/netflow/detail/browse")
    @ResponseBody
    //@MwPermit(moduleName = "netflow_manage")
    public ResponseBase getNetFlowDetail(@RequestBody NetFlowDetailParam param) {
        Reply reply;
        try {
            reply = mwNetflowService.getNetFlowDetail(param);
        } catch (Exception e) {
            log.error("getNetflowDetail error", e);
            return setResultFail("流量明细获取数据失败", null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 编辑前获取监控接口数据
     */
    @ApiOperation(value = "流量明细获取图表数据")
    @PostMapping("/netflow/detail/chart/browse")
    @ResponseBody
    //@MwPermit(moduleName = "netflow_manage")
    public ResponseBase getNetFlowDetailChart(@RequestBody NetFlowDetailParam param) {
        Reply reply;
        try {
            reply = mwNetflowService.getNetFlowDetailChart(param);
        } catch (Exception e) {
            log.error("getNetflowDetail error", e);
            return setResultFail("流量明细获取数据失败", null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 编辑前获取监控接口数据
     */
    @ApiOperation(value = "流量明细获取数据栏目")
    @PostMapping("/netflow/detail/getColumns")
    @ResponseBody
    //@MwPermit(moduleName = "netflow_manage")
    public ResponseBase getNetFlowDetailTest(@RequestBody NetFlowDetailParam param) {
        Reply reply;
        try {
            reply = mwNetflowService.getNetFlowColumns();
        } catch (Exception e) {
            log.error("getNetflowDetail error", e);
            return setResultFail("流量明细获取数据失败", null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 获取流量明细缓存数据
     *
     * @param param
     */
    @ApiOperation(value = "获取流量明细缓存数据")
    @PostMapping("/netflow/detail/getNetFlowDetailCacheInfo")
    @ResponseBody
    //@MwPermit(moduleName = "netflow_manage")
    public ResponseBase getNetFlowDetailCacheInfo(@RequestBody NetflowDetailCacheInfo param) {
        Reply reply;
        try {
            reply = mwNetflowService.getNetFlowDetailCacheInfo();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Exception e) {
            log.error("getNetflowDetail error", e);
            return setResultFail("获取失败", null);
        }
        return setResultSuccess(reply);
    }


    /**
     * 保存流量分析已选择的数据
     *
     * @param param
     */
    @ApiOperation(value = "保存流量分析已选择的数据")
    @PostMapping("/netflow/detail/saveSelectedColumns")
    @ResponseBody
    //@MwPermit(moduleName = "netflow_manage")
    public ResponseBase saveNetFlowDetailSelectedColumns(@RequestBody NetflowDetailCacheInfo param) {
        Reply reply;
        try {
            reply = mwNetflowService.saveNetFlowDetailSelectedColumns(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Exception e) {
            log.error("saveNetFlowDetailSelectedColumns error", e);
            return setResultFail("保存失败", null);
        }
        return setResultSuccess(reply);
    }
}
