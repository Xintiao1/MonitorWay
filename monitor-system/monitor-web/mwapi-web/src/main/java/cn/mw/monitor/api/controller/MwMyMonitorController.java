package cn.mw.monitor.api.controller;


import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.server.param.QueryAdvanceTableParam;
import cn.mw.monitor.server.param.QueryArgumentsParam;
import cn.mw.monitor.server.param.QueryComLayoutParam;
import cn.mw.monitor.server.param.QueryComLayoutVersionParam;
import cn.mw.monitor.server.serverdto.ComponentLayoutDTO;
import cn.mw.monitor.server.service.MwMyMonitorService;
import cn.mw.monitor.service.server.api.dto.AdvanceTableDTO;
import cn.mw.monitor.service.server.api.dto.ItemBaseDTO;
import cn.mw.monitor.service.server.api.dto.LineChartDTO;
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
 * @Date 2021/2/3 15:52
 * @Version 1.0
 */
@RequestMapping("/mwapi/myMonitor")
@Controller
@Api(value = "我的监测", tags = "详情页可视化")
public class MwMyMonitorController extends BaseApiService {
    private static final Logger logger = LoggerFactory.getLogger("control-MwMyMonitorController" + MwMyMonitorController.class.getName());

    @Autowired
    private MwMyMonitorService myMonitorService;

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getItemList/browse")
    @ResponseBody
    @ApiOperation(value = "查询筛选后的监控项集合")
    public ResponseBase getItemList(@RequestBody QueryArgumentsParam queryArgumentsParam) {
        Reply reply;
        try {
            reply = myMonitorService.getItemInfoByFilter(queryArgumentsParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(ErrorConstant.MYMONITOR_SELECT_ITEMS_INFO_MSG_302021, queryArgumentsParam);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getComponentList/browse")
    @ResponseBody
    @ApiOperation(value = "查询基础组件集合")
    public ResponseBase getComponentList() {
        Reply reply;
        try {
            reply = myMonitorService.getComponentList();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(ErrorConstant.MYMONITOR_SELECT_COMPONENTS_INFO_MSG_302022, null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getBaseLineCharts")
    @ResponseBody
    @ApiOperation(value = "查询折线图数据")
    public ResponseBase getBaseLineCharts(@RequestBody LineChartDTO lineChartDTO) {
        Reply reply;
        try {
            if (lineChartDTO.getDateType() == 5) {
                if (lineChartDTO.getDateStart() == null || lineChartDTO.getDateEnd() == null) {
                    return setResultFail("自定义时间不能为空", Reply.fail(lineChartDTO));
                }
            }
            reply = myMonitorService.getLineChartsData(lineChartDTO);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("getBaseLineCharts",e);
            return setResultFail(ErrorConstant.MYMONITOR_SELECT_LINECHART_INFO_MSG_302020, lineChartDTO);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/saveComponentLayout")
    @ResponseBody
    @ApiOperation(value = "保存详情页布局")
    public ResponseBase saveComponentLayout(@RequestBody ComponentLayoutDTO aParam) {
        Reply reply;
        try {
            reply = myMonitorService.saveComponentLayout(aParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(ErrorConstant.MYMONITOR_SAVE_COMPONENT_INFO_MSG_302019, aParam);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/selectComponentLayout")
    @ResponseBody
    @ApiOperation(value = "查询详情页布局")
    public ResponseBase selectComponentLayout(@RequestBody QueryComLayoutParam qParam) {
        Reply reply;
        try {
            reply = myMonitorService.selectComponentLayout(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(ErrorConstant.MYMONITOR_SELECT_COMPONENTLAYOUT_INFO_MSG_302023, qParam);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/selectComLayout/version")
    @ResponseBody
    @ApiOperation(value = "查询历史版本的布局数据")
    public ResponseBase selectComLayoutByVersion(@RequestBody QueryComLayoutVersionParam param) {
        Reply reply;
        try {
            reply = myMonitorService.selectComLayoutByVersion(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(ErrorConstant.MYMONITOR_SELECT_COMPONENTLAYOUT_INFO_MSG_302028, param);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getItemTOP5List/browse")
    @ResponseBody
    @ApiOperation(value = "查询某监控项的排行")
    public ResponseBase getItemTOP5List(@RequestBody ItemBaseDTO param) {
        Reply reply;
        try {
            reply = myMonitorService.getItemRank(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(e.getMessage(), param);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getItemTableList")
    @ResponseBody
    @ApiOperation(value = "查询基础表格信息")
    public ResponseBase getItemTableList(@RequestBody ItemBaseDTO param) {
        Reply reply;
        try {
            reply = myMonitorService.getItemsTableInfo(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(e.getMessage(), param);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getBarGraphInfo")
    @ResponseBody
    @ApiOperation(value = "柱状图信息条数控制在12条以下")
    public ResponseBase getBarGraphInfo(@RequestBody LineChartDTO param) {
        Reply reply;
        try {
            reply = myMonitorService.getBarGraphInfo(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(e.getMessage(), param);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getPieChartInfo")
    @ResponseBody
    @ApiOperation(value = "磁盘信息饼状图")
    public ResponseBase getPieChartInfo(@RequestBody LineChartDTO param) {
        Reply reply;
        try {
            reply = myMonitorService.getPieChartInfo(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(e.getMessage(), param);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getPieChartData")
    @ResponseBody
    @ApiOperation(value = "查询基础表格信息")
    public ResponseBase getPieChartData(@RequestBody ItemBaseDTO param) {
        Reply reply;
        try {
            reply = myMonitorService.getPieChartData(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(e.getMessage(), param);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getApplicationList/browse")
    @ResponseBody
    @ApiOperation(value = "高级表格查询所有应用集list")
    public ResponseBase getApplicationList(@RequestBody QueryAdvanceTableParam param) {
        Reply reply;
        try {
            reply = myMonitorService.getApplicationList(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(ErrorConstant.MYMONITOR_SELECT_ITEMS_INFO_MSG_302025, param);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getItemListByApplication/browse")
    @ResponseBody
    @ApiOperation(value = "高级表格根据应用集查询监控项和监控设备list")
    public ResponseBase getItemListByApplication(@RequestBody QueryAdvanceTableParam param) {
        Reply reply;
        try {
            reply = myMonitorService.getItemListByApplication(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(ErrorConstant.MYMONITOR_SELECT_ITEMS_INFO_MSG_302021, param);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getAdvanceTableInfo/browse")
    @ResponseBody
    @ApiOperation(value = "高级表格查询数据")
    public ResponseBase getAdvanceTableInfo(@RequestBody AdvanceTableDTO param) {
        Reply reply;
        try {
            reply = myMonitorService.getAdvanceTableInfo(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Exception e) {
            logger.error("高级表格查询数据失败："+e.getMessage());
            return setResultFail(ErrorConstant.MYMONITOR_SELECT_ITEMS_INFO_MSG_302026, param);
        }
        return setResultSuccess(reply);
    }
}
