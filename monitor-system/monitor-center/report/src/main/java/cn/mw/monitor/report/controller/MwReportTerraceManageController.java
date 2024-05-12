package cn.mw.monitor.report.controller;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.report.dto.TrendParam;
import cn.mw.monitor.report.dto.assetsdto.IpReportSreach;
import cn.mw.monitor.report.dto.assetsdto.RunTimeQueryParam;
import cn.mw.monitor.report.param.*;
import cn.mw.monitor.report.service.MWRreportExportService;
import cn.mw.monitor.report.service.MwReportTerraceManageService;
import cn.mw.monitor.service.assets.param.QueryTangAssetsParam;
import cn.mw.monitor.service.server.api.dto.ServerHistoryDto;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import com.github.pagehelper.PageInfo;
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

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ClassName MwReportTerraceManageController
 * @Description ToDo
 * @Author gengjb
 * @Date 2021/10/13 10:34
 * @Version 1.0
 **/
@RequestMapping("/mwapi")
@Slf4j
@Controller
@Api(value = "山鹰报表平台管理", tags = "")
public class MwReportTerraceManageController extends BaseApiService {

    private static final Logger logger = LoggerFactory.getLogger("control-" + MwReportTerraceManageController.class.getName());

    @Autowired
    private MwReportTerraceManageService terraceManageService;

    @Autowired
    private MWRreportExportService exportService;



    /**
     * 山鹰资产信息查询
     * @param browseTangAssetsParam
     * @return
     */
    @MwPermit(moduleName = "report_manage")
    @PostMapping("/report/assets/browse")
    @ResponseBody
    @ApiOperation(value = "资产信息报表查询")
    public ResponseBase selectReportAssetsNews(@RequestBody QueryTangAssetsParam browseTangAssetsParam) {
        Reply reply;
        try {
            reply = terraceManageService.selectAssetsNews(browseTangAssetsParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("查询资产信息报表失败browse{}", e);
            return setResultFail("查询资产信息报表失败","");
        }
        return setResultSuccess(reply);
    }


    /**
     * 山鹰CPU报表信息查询
     * @param param
     * @return
     */
    @MwPermit(moduleName = "report_manage")
    @PostMapping("/report/cpunews/browse")
    @ResponseBody
    @ApiOperation(value = "CPU信息报表查询")
    public ResponseBase selectReportCPUNews(@RequestBody RunTimeQueryParam param) {
        Reply reply;
        try {
            reply = terraceManageService.selectReportCPUNews(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("CPU信息报表查询失败{}", e);
            return setResultFail("CPU信息报表查询失败","");
        }
        return setResultSuccess(reply);
    }



    @PostMapping("/report/ip/browse")
    @ResponseBody
    @ApiOperation(value = "ip报表查询")
    public ResponseBase selectReportIpNews(@RequestBody IpReportSreach param) {
        Reply reply;
        try {
            reply = terraceManageService.selectReportIpNews(param);

        } catch (Throwable e) {
            logger.error("ip报表查询报表失败browse{}", e);
            return setResultFail("ip报表查询失败","");
        }
        return setResultSuccess(reply);
    }


    /**
     * 报表导出
     */
    @MwPermit(moduleName = "report_manage")
    @PostMapping("/report/ipReport/export")
    @ResponseBody
    @ApiOperation(value = "ip报表导出")
    public void ipReporyExport(@RequestBody IpReportSreach param, HttpServletResponse response) {
        try {
            param.setPageSize(0);
            param.setPageSize(100000);
            PageInfo pageInfo =(PageInfo) terraceManageService.selectReportIpNews(param).getData();
            List<IpAddressReport> ipReportSreaches= pageInfo.getList();
            List<IpAddressReport> ipAddressReportList=new ArrayList<>();
            for (IpAddressReport i:ipReportSreaches) {
                if (i.getUseStatus()==0){
                    i.setStrUseStatus("未使用");
                }
                if (i.getUseStatus()!=0){
                    i.setStrUseStatus("已使用");
                }
                if (i.getOnLineType()==0){
                    i.setStrOnLineType("离线");
                }
                if (i.getOnLineType()!=0){
                    i.setStrOnLineType("在线");
                }
                ipAddressReportList.add(i);
            }
            terraceManageService.ipReportExport(ipAddressReportList,response,param.getRadio());
        } catch (Throwable e) {
            logger.error("ip报表导出失败browse{}", e);
        }
    }


    /**
     * 线路报表信息查询
     * @param trendParam
     * @return
     */
    @MwPermit(moduleName = "report_manage")
    @PostMapping("/report/linknews/browse")
    @ResponseBody
    @ApiOperation(value = "线路流量信息报表查询")
    public ResponseBase selectReportLinkNews(@RequestBody TrendParam trendParam) {
        Reply reply;
        try {
            reply = terraceManageService.selectReportLinkNews(trendParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("线路流量信息报表查询失败browse{}", e);
            return setResultFail("线路流量信息报表查询失败","");
        }
        return setResultSuccess(reply);
    }


    /**
     * 磁盘使用率报表信息查询
     * @param trendParam
     * @return
     */
    @MwPermit(moduleName = "report_manage")
    @PostMapping("/report/diskuse/browse")
    @ResponseBody
    @ApiOperation(value = "磁盘使用率报表查询")
    public ResponseBase selectReportDiskUse(@RequestBody TrendParam trendParam) {
        Reply reply;
        try {
            reply = terraceManageService.selectReportDiskUse(trendParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("磁盘使用率报表查询失败browse{}", e);
            return setResultFail("磁盘使用率报表查询失败","");
        }
        return setResultSuccess(reply);
    }

//    /**
//     * 山鹰磁盘使用率报表信息查询
//     * @param trendParam
//     * @return
//     */
//    @MwPermit(moduleName = "report_manage")
//    @PostMapping("/report/diskuseable/browse")
//    @ResponseBody
//    @ApiOperation(value = "磁盘可用率报表查询")
//    public ResponseBase selectReportDiskUsable(@RequestBody TrendParam trendParam) {
//        Reply reply;
//        try {
//            reply = terraceManageService.selectReportDiskUsable(trendParam);
//            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
//                return setResultFail(reply.getMsg(), reply.getData());
//            }
//        } catch (Throwable e) {
//            logger.error("查询山鹰资产信息报表失败browse{}", e);
//            return setResultFail(e.getMessage(), trendParam);
//        }
//        return setResultSuccess(reply);
//    }

    /**
     * 资产可用性报表信息查询
     * @param param
     * @return
     */
    @MwPermit(moduleName = "report_manage")
    @PostMapping("/report/assetsUsability/browse")
    @ResponseBody
    @ApiOperation(value = "资产可用性报表查询")
    public ResponseBase selectReportAssetsUsability(@RequestBody RunTimeQueryParam param) {
        Reply reply;
        try {

            reply = terraceManageService.selectReportAssetsUsability(param,false);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("资产可用性报表查询失败browse{}", e);
            return setResultFail("资产可用性报表查询失败","");
        }
        return setResultSuccess(reply);
    }

    /**
     * 山鹰资产信息报表导出
     */
    @MwPermit(moduleName = "report_manage")
    @PostMapping("/report/assets/export")
    @ResponseBody
    @ApiOperation(value = "资产信息报表导出")
    public void assetsNewsReportExport(@RequestBody List<AssetsNewsReportExportParam> assets, HttpServletResponse response) {
        try {
            terraceManageService.assetsNewsReportExport(assets,response);
        } catch (Throwable e) {
            logger.error("导出资产信息报表失败browse{}", e);
        }
    }




    /**
     * 山鹰CPU报表信息导出
     */
    @MwPermit(moduleName = "report_manage")
    @PostMapping("/report/cpunews/export")
    @ResponseBody
    @ApiOperation(value = "CPU信息报表导出")
    public void cpuNewsReportExport(@RequestBody List<CpuNewsReportExportParam> cpuParams, HttpServletResponse response) {
        try {
            terraceManageService.cpuNewsReportExport(cpuParams,response);
        } catch (Throwable e) {
            logger.error("导出CPU信息报表失败browse{}", e);
        }
    }


    /**
     * 磁盘使用率导出
     */
    @MwPermit(moduleName = "report_manage")
    @PostMapping("/report/diskuse/export")
    @ResponseBody
    @ApiOperation(value = "磁盘使用率报表导出")
    public void diskUseReportExport(@RequestBody List<DiskUseReportExportParam> params, HttpServletResponse response) {
        try {
            terraceManageService.diskUseReportExport(params,response);
        } catch (Throwable e) {
            logger.error("导出磁盘使用率报表失败browse{}", e);
        }
    }



    /**
     * 磁盘使用率导出
     */
    @MwPermit(moduleName = "report_manage")
    @PostMapping("/report/diskuseable/export")
    @ResponseBody
    @ApiOperation(value = "磁盘可用率报表导出")
    public void diskUseAbleReportExport(@RequestBody List<DiskUseAbleReportExportParam> params, HttpServletResponse response) {
        try {
            terraceManageService.diskUseAbleReportExport(params,response);
        } catch (Throwable e) {
            logger.error("导出磁盘可用率报表失败browse{}", e);
        }
    }


    /**
     * 资产可用性报表导出
     */
    @MwPermit(moduleName = "report_manage")
    @PostMapping("/report/assetsUsability/export")
    @ResponseBody
    @ApiOperation(value = "资产可用性报表导出")
    public void assetsUsabilityReportExport(@RequestBody List<MwAssetsUsabilityParam> params, HttpServletResponse response) {
        try {
            terraceManageService.assetsUsabilityReportExport(params,response);
        } catch (Throwable e) {
            logger.error("导出资产可用性报表失败browse{}", e);
        }
    }




//    @MwPermit(moduleName = "report_manage")
//    @GetMapping("/report/test/export")
//    @ApiOperation(value = "资产可用性报表导出")
//    public void assetsUsabilityReportExport(HttpServletResponse response) {
//        try {
//            MwLineMplsParam param = new MwLineMplsParam();
//            param.setDateType(1);
//            param.setType(1);
//            MwLineMplsParam param2 = new MwLineMplsParam();
//            param2.setDateType(1);
//            param2.setType(2);
//            List<MwLineMplsParam> params = new ArrayList<>();
//            params.add(param);
//            params.add(param2);
//            terraceManageService.lineMplsReportExport(params,response);
//        } catch (Throwable e) {
//            logger.error("导出山鹰磁盘可用率报表失败browse{}", e);
//        }
//    }



    /**
     * 线路流量报表导出
     */
    @MwPermit(moduleName = "report_manage")
    @PostMapping("/report/lineflow/export")
    @ResponseBody
    @ApiOperation(value = "线路流量报表导出")
    public void lineFlowReportExport(@RequestBody List<LineFlowReportParam> params, HttpServletResponse response) {
        try {
            terraceManageService.lineFlowReportExport(params,response);
        } catch (Throwable e) {
            logger.error("导出线路流量报表失败browse{}", e);
        }
    }

    /**
     * 磁盘使用率导出
     */
    @MwPermit(moduleName = "report_manage")
    @PostMapping("/report/assets/getTree")
    @ResponseBody
    @ApiOperation(value = "查询资产树状结构分类数据")
    public ResponseBase selectAssetsReportTree(@RequestBody TrendParam param) {
        Reply reply;
        try {
            reply =  terraceManageService.selectAssetsReportTree(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("报表查询资产树状结构数据失败browse{}", e);
            return setResultFail("查询资产树状结构分类数据失败","");
        }
        return setResultSuccess(reply);
    }

    /**
     * 线路MPLS报告报表
     * @param param
     * @return
     */
    @MwPermit(moduleName = "report_manage")
    @PostMapping("/report/lineMpls/browse")
    @ResponseBody
    @ApiOperation(value = "线路MPLS报表查询")
    public ResponseBase selectReportLineMpls(@RequestBody MwLineMplsParam param) {
        Reply reply;
        try {
            reply = terraceManageService.selectReportLineMpls(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("线路MPLS报表查询失败browse{}", e);
            return setResultFail("线路MPLS报表查询失败","");
        }
        return setResultSuccess(reply);
    }

    /**
     * 线路名称的下拉数据
     * @return
     */
    @MwPermit(moduleName = "report_manage")
    @PostMapping("/report/linkName/browse")
    @ResponseBody
    @ApiOperation(value = "线路名称查询")
    public ResponseBase selectLineMplsReportLineName() {
        Reply reply;
        try {
            reply = terraceManageService.seleAllLink();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("线路名称查询失败browse{}", e);
            return setResultFail("线路名称查询失败","");
        }
        return setResultSuccess(reply);
    }


    /**
     * 线路汇总报表
     * @param param
     * @return
     */
    @MwPermit(moduleName = "report_manage")
    @PostMapping("/report/linepool/browse")
    @ResponseBody
    @ApiOperation(value = "线路MPLS报表汇总数据查询")
    public ResponseBase selectReportLineMplsPool(@RequestBody List<ServerHistoryDto> param) {
        Reply reply;
        try {
            reply = terraceManageService.selectReportLineMplsPool(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("线路MPLS报表查询失败browse{}", e);
            return setResultFail("线路MPLS报表汇总数据查询失败","");
        }
        return setResultSuccess(reply);
    }

    /**
     * 山鹰线路汇总报表
     * @param
     * @return
     */
    @MwPermit(moduleName = "report_manage")
    @PostMapping("/report/lineMpls/export")
    @ResponseBody
    @ApiOperation(value = "线路MPLS报表导出")
    public void lineMplsReportExport(@RequestBody List<MwLineMplsParam> params, HttpServletResponse response) {
        Reply reply;
        try {
            terraceManageService.lineMplsReportExport(params,response);
        } catch (Throwable e) {
            logger.error("线路MPLS报表导出失败", e);
        }

    }

    /**
     * 线路分级查询
     * @param
     * @return
     */
    @MwPermit(moduleName = "report_manage")
    @PostMapping("/report/linkgrade/browse")
    @ResponseBody
    @ApiOperation(value = "线路MPLS报表线路分级查询")
    public ResponseBase selectReportLinkGrade() {
        Reply reply;
        try {
            reply = terraceManageService.selectReportLinkGrade();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("线路MPLS报表线路分级查询失败browse{}", e);
            return setResultFail("线路MPLS报表线路分级查询失败","");
        }
        return setResultSuccess(reply);
    }

    /**
     * 线路分级查询
     * @param
     * @return
     */
    @MwPermit(moduleName = "report_manage")
    @PostMapping("/report/browse/getGradeData")
    @ResponseBody
    @ApiOperation(value = "线路MPLS报表线路分级查询线路数据")
    public ResponseBase selectReportLinkGradeData(@RequestBody MwLinkGradeParam param) {
        Reply reply;
        try {
            reply = terraceManageService.selectReportLinkGradeData(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("线路MPLS报表线路分级查询线路数据失败browse{}", e);
            return setResultFail("线路MPLS报表线路分级查询线路数据失败","");
        }
        return setResultSuccess(reply);
    }

    /**
     * 线路分级查询
     * @param
     * @return
     */
    @MwPermit(moduleName = "report_manage")
    @PostMapping("/report/linkhistory/browse")
    @ResponseBody
    @ApiOperation(value = "查询线路历史流量数据")
    public ResponseBase selectLinkHistoryFlow(@RequestBody ServerHistoryDto param) {
        Reply reply;
        try {
            reply = terraceManageService.selectLinkHistoryFlow(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("查询线路历史流量数据失败browse{}", e);
            return setResultFail("查询线路历史流量数据失败","");
        }
        return setResultSuccess(reply);
    }



    /**
     * 磁盘使用率导出
     */
    @MwPermit(moduleName = "report_manage")
    @PostMapping("/report/diskuse/allExport")
    @ResponseBody
    @ApiOperation(value = "磁盘使用率报表全部导出")
    public void diskUseReportAllExport(@RequestBody TrendParam trendParam, HttpServletResponse response) {
        try {
            exportService.diskUseReportAllExport(trendParam,response);
        } catch (Throwable e) {
            logger.error("全部导出磁盘使用率报表失败browse{}", e);
        }
    }


    /**
     * CPU报表信息全部导出
     */
    @MwPermit(moduleName = "report_manage")
    @PostMapping("/report/cpunews/allExport")
    @ResponseBody
    @ApiOperation(value = "CPU信息报表全部导出导出")
    public void cpuNewsReportAllExport(@RequestBody RunTimeQueryParam param, HttpServletResponse response) {
        try {
            exportService.cpuNewsReportAllExport(param,response);
        } catch (Throwable e) {
            logger.error("导出CPU信息报表全部数据失败browse{}", e);
        }
    }


    /**
     * 资产可用性全部数据导出
     */
    @MwPermit(moduleName = "report_manage")
    @PostMapping("/report/assetsUsability/allExport")
    @ResponseBody
    @ApiOperation(value = "资产可用性报表全部导出")
    public void assetsUsabilityReportAllExport(@RequestBody RunTimeQueryParam param, HttpServletResponse response) {
        try {
            exportService.assetsUsabilityReportAllExport(param,response);
        } catch (Throwable e) {
            logger.error("导出资产可用性报表失败browse{}", e);
        }
    }


    /**
     * 线路流量报表全部导出
     */
    @MwPermit(moduleName = "report_manage")
    @PostMapping("/report/lineflow/allExport")
    @ResponseBody
    @ApiOperation(value = "线路流量报表全部导出")
    public void lineFlowReportAllExport(@RequestBody TrendParam trendParam, HttpServletResponse response) {
        try {
            exportService.lineFlowReportAllExport(trendParam,response);
        } catch (Throwable e) {
            logger.error("导出线路流量报表全部数据失败browse{}", e);
        }
    }



    @MwPermit(moduleName = "report_manage")
    @PostMapping("/report/dropDown/browse")
    @ResponseBody
    @ApiOperation(value = "获取手动执行定时任务下拉框数据")
    public ResponseBase getReportDown() {
        Reply reply;
        try {
            reply =  terraceManageService.selectReportDown();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("查询报表下拉数据失败", e);
            return setResultFail("查询报表下拉数据失败失败","");
        }
        return setResultSuccess(reply);
    }

    /**
     * 手动执行选择的定时任务
     */
    @MwPermit(moduleName = "report_manage")
    @PostMapping("/report/manual/timeTaskRun")
    @ResponseBody
    @ApiOperation(value = "手动执行对应定时任务")
    public ResponseBase runTimeTask(@RequestBody Map<String,Integer> reportMap) {
        Reply reply;
        try {
            Integer reportId = reportMap.get("reportId");
            reply =  terraceManageService.manualRunTimeTask(reportId);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("查询报表下拉数据失败", e);
            return setResultFail("手动执行对应定时任务失败","");
        }
        return setResultSuccess(reply);
    }

    /**
     * 查询资产以及资产对应接口数据
     */
//    @MwPermit(moduleName = "report_manage")
//    @PostMapping("/report/assetsInterface/browse")
//    @ResponseBody
//    @ApiOperation(value = "查询资产接口数据")
//    public ResponseBase selectAseestsInterface() {
//        Reply reply;
//        try {
//            reply =  terraceManageService.selectAseestsInterface();
//            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
//                return setResultFail(reply.getMsg(), reply.getData());
//            }
//        } catch (Throwable e) {
//            logger.error("查询报表下拉数据失败", e);
//            return setResultFail(e.getMessage(),"查询失败");
//        }
//        return setResultSuccess(reply);
//    }

    /**
     * 查询资产以及资产对应接口数据
     */
    @MwPermit(moduleName = "report_manage")
    @PostMapping("/report/lylLinkFlow/browse")
    @ResponseBody
    @ApiOperation(value = "查询流量数据")
    public ResponseBase selectLylLinkFlowData(@RequestBody TrendParam param) {
        Reply reply;
        try {
            reply =  terraceManageService.selectLylLinkFlowData(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("查询流量报表失败", e);
            return setResultFail("查询流量报表失败","");
        }
        return setResultSuccess(reply);
    }

    /**
     * 资产可用性全部数据导出
     */
    @MwPermit(moduleName = "report_manage")
    @PostMapping("/report/assetsStatusDelayed/export")
    @ResponseBody
    @ApiOperation(value = "导出每个资产的状态和延迟数据")
    public void assetsStatusDelayedExport(HttpServletResponse response) {
        try {
            exportService.assetsStatusDelayedExport(response);
        } catch (Throwable e) {
            logger.error("导出每个资产的状态和延迟数据失败browse{}", e);
        }
    }
}
