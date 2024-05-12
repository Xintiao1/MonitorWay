package cn.mw.monitor.api.controller;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.SpringUtils;
import cn.mw.monitor.report.dto.assetsdto.PeriodTrendDto;
import cn.mw.monitor.report.dto.assetsdto.RunTimeItemValue;
import cn.mw.monitor.report.dto.assetsdto.RunTimeQueryParam;
import cn.mw.monitor.report.service.MwSyReportService;
import cn.mw.monitor.report.service.manager.RunTimeReportManager;
import cn.mw.monitor.report.util.ReportDateUtil;
import cn.mw.monitor.state.DateTimeTypeEnum;
import cn.mw.monitor.state.RuntimeReportState;
import cn.mw.monitor.util.RedisUtils;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.report.dto.linkdto.ExportLinkParam;
import cn.mw.monitor.report.param.EditorTimeParam;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.report.dto.CreatAndUpdateReportParam;
import cn.mw.monitor.report.dto.DeleteParam;
import cn.mw.monitor.report.dto.QueryReportParam;
import cn.mw.monitor.report.dto.TrendParam;
import cn.mw.monitor.report.param.ExcelReportParam;
import cn.mw.monitor.report.param.ReportCountParam;
import cn.mw.monitor.report.service.MwReportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * @author xhy
 * @date 2020/5/9 15:59
 */
@RequestMapping("/mwapi/report")
@Controller
@Api(value = "报表", tags = "报表")
@Slf4j
public class MWReportController extends BaseApiService {
    private static final Logger logger = LoggerFactory.getLogger("control-" + MWReportController.class.getName());

    @Autowired
    private MwReportService mwreportService;
    @Autowired
    private MwSyReportService mwSyReportService;
    @Autowired
    RunTimeReportManager runTimeReportManager;

    @MwPermit(moduleName = "report_manage")
    @PostMapping("/getReportCount/browse")
    @ResponseBody
    @ApiOperation(value = "根据用户权限和报表类型查询报表数量")
    public ResponseBase getReportCount(@RequestBody ReportCountParam reportCountParam) {
        Reply reply;
        try {
            reply = mwreportService.getReportCount(reportCountParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("getReportCount{}", e);
            return setResultFail(e.getMessage(), ErrorConstant.REPORT_TYPE_MSG_303004);
        }

        return setResultSuccess(reply);

    }

    @MwPermit(moduleName = "report_manage")
    @PostMapping("/getReportType/browse")
    @ResponseBody
    @ApiOperation(value = "查询报表类型url")
    public ResponseBase getReportType() {
        Reply reply;
        try {
            reply = mwreportService.getReportType();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("getReportType{}", e);
            return setResultFail(e.getMessage(), ErrorConstant.REPORT_TYPE_MSG_303004);
        }

        return setResultSuccess(reply);

    }

//    @PostMapping("/getReportTypeCount/browse")
//    @ResponseBody
//    @ApiOperation(value = "查询报表类型url")
//    public ResponseBase getReportTypeCount(@RequestBody QueryReportParam queryReportParam) {
//        Reply reply;
//        try {
//            reply = mwreportService.getReportTypeCount(queryReportParam.getReportTypeId());
//            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
//                return setResultFail(reply.getMsg(), reply.getData());
//            }
//        } catch (Throwable e) {
//            logger.error("getReportType{}", e);
//            return setResultFail(e.getMessage(), ErrorConstant.REPORT_TYPE_MSG_303004);
//        }
//
//        return setResultSuccess(reply);
//
//    }

    @MwPermit(moduleName = "report_manage")
    @PostMapping("/getReportTimeTask/browse")
    @ResponseBody
    @ApiOperation(value = "查询报表定时任务url")
    public ResponseBase getReportTimeTask() {
        Reply reply;
        try {
            reply = mwreportService.getReportTimeTask();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("getReportTimeTask{}", e);
            return setResultFail(e.getMessage(), ErrorConstant.REPORT_TIME_TASK_MSG_303005);
        }

        return setResultSuccess(reply);

    }

    @MwPermit(moduleName = "report_manage")
    @PostMapping("/getReportAction/browse")
    @ResponseBody
    @ApiOperation(value = "查询报表通知类型url")
    public ResponseBase getReportAction() {
        Reply reply;
        try {
            reply = mwreportService.getReportAction();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("getReportAction{}", e);
            return setResultFail(e.getMessage(), ErrorConstant.REPORT_ACTION_MSG_303010);
        }
        return setResultSuccess(reply);

    }

    /**
     * 用户设置查询时间段
     */
    @MwPermit(moduleName = "report_manage")
    @PostMapping("/editorTime")
    @ResponseBody
    public ResponseBase editorTime(@RequestBody EditorTimeParam param, HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwreportService.editorTime(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("editorTime{}", e);
            return setResultFail(e.getMessage(), param);
        }

        return setResultSuccess(reply);
    }


    /**
     * 用户查询设置的时间段
     */
    @MwPermit(moduleName = "report_manage")
    @PostMapping("/selectTime")
    @ResponseBody
    public ResponseBase selectTime(@RequestBody EditorTimeParam param, HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwreportService.selectTime(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("selectTime{}", e);
            return setResultFail(e.getMessage(), ErrorConstant.SOLAR_TIME_SELECT_MSG_306004);
        }

        return setResultSuccess(reply);
    }

    /**
     * 时间段下拉框的url
     */
    @MwPermit(moduleName = "report_manage")
    @PostMapping("/selectDayType")
    @ResponseBody
    public ResponseBase selectDayType(@RequestBody EditorTimeParam param, HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwreportService.selectDayType(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("selectDayType{}", e);
            return setResultFail(e.getMessage(), ErrorConstant.SOLAR_TIME_SELECT_MSG_306004);
        }

        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "report_manage")
    @PostMapping("/create")
    @ResponseBody
    @ApiOperation(value = "报表新增")
    public ResponseBase creatReport(@RequestBody CreatAndUpdateReportParam creatAndUpdateReportParam) {
        Reply reply;
        try {
            reply = mwreportService.creatReport(creatAndUpdateReportParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("creatReport{}", e);
            return setResultFail(e.getMessage(), creatAndUpdateReportParam);
        }

        return setResultSuccess(reply);

    }

    @MwPermit(moduleName = "report_manage")
    @PostMapping("/perform")
    @ResponseBody
    @ApiOperation(value = "报表修改前查询")
    public ResponseBase selectById(@RequestBody CreatAndUpdateReportParam creatAndUpdateReportParam) {
        Reply reply;
        try {
            reply = mwreportService.selectById(creatAndUpdateReportParam.getReportId());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("selectById{}", e);
            return setResultFail(e.getMessage(), creatAndUpdateReportParam);
        }

        return setResultSuccess(reply);

    }

    @MwPermit(moduleName = "report_manage")
    @PostMapping("/editor")
    @ResponseBody
    @ApiOperation(value = "报表修改")//修改分为当个修改和批量修改
    public ResponseBase updateReport(@RequestBody CreatAndUpdateReportParam creatAndUpdateReportParam) {
        Reply reply;
        try {
            reply = mwreportService.updateReport(creatAndUpdateReportParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("updateReport{}", e);
            return setResultFail(e.getMessage(), creatAndUpdateReportParam);
        }

        return setResultSuccess(reply);

    }

    @MwPermit(moduleName = "report_manage")
    @PostMapping("/delete")
    @ResponseBody
    @ApiOperation(value = "报表刪除")
    public ResponseBase deleteReport(@RequestBody DeleteParam deleteParam) {
        Reply reply;
        try {
            reply = mwreportService.deleteReport(deleteParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("deleteReport{}", e);
            return setResultFail(e.getMessage(), deleteParam);
        }

        return setResultSuccess(reply);

    }

    @MwPermit(moduleName = "report_manage")
    @PostMapping("/runtimeOfAsset/browse")
    @ResponseBody
    @ApiOperation(value = "运行状态报表资产统计查询报表查询")
    public ResponseBase selectRunTimeTest(@RequestBody RunTimeQueryParam param) {
        Reply reply;
        try {
            reply = mwreportService.getRunTimeReportOfAeest(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("browse{}", e);
            return setResultFail(e.getMessage(), param);
        }

        return setResultSuccess(reply);

    }


    @MwPermit(moduleName = "report_manage")
    @PostMapping("/runtimeOfTopN/browse")
    @ResponseBody
    @ApiOperation(value = "运行状态报表topN查询")
    public ResponseBase selectRunTimeTest2(@RequestBody RunTimeQueryParam param) {
        Reply reply = null;
        try {
            Integer datesize = param.getDataSize();
            param.setTimingType(false);
            //放开全部
            param.setDataSize(1000000);
            //cpu,内存，丢包率
            if (param.getReportItemType() == RuntimeReportState.CPU_MEMORY_LOSS.getCode()) {
                reply = mwreportService.getRunTimeItemUtilization(param);
                List<RunTimeItemValue> list = (List<RunTimeItemValue>) reply.getData();
                List<RunTimeItemValue> lists = new ArrayList<>();
                if (param.getSearchName() != null && !param.getSearchName().trim().equals("")) {
                    for (RunTimeItemValue r : list) {
                        if (r.getAssetName().contains(param.getSearchName())) {
                            lists.add(r);
                        }
                    }
                } else {
                    lists.addAll(list);
                }
                lists = lists.subList(0, datesize == 0 || datesize > lists.size() ? lists.size() : datesize);
                return setResultSuccess(Reply.ok(lists));
            }
            //接口，磁盘
            if (param.getReportItemType() == RuntimeReportState.INTERFACE_DISK.getCode()) {
                reply = mwreportService.getRunTimeItem(param);
                List<RunTimeItemValue> list = (List<RunTimeItemValue>) reply.getData();
                List<RunTimeItemValue> lists = new ArrayList<>();
                if (param.getSearchName() != null && !param.getSearchName().trim().equals("")) {
                    for (RunTimeItemValue r : list) {
                        if (r.getAssetName().contains(param.getSearchName())) {
                            lists.add(r);
                        }
                    }
                } else {
                    lists.addAll(list);
                }
                lists = lists.subList(0, datesize == 0 || datesize > lists.size() ? lists.size() : datesize);
                return setResultSuccess(Reply.ok(lists));
            }
            //资产可用性
            if (param.getReportItemType() == RuntimeReportState.ASSETUTILIZATION.getCode()) {
                reply = mwreportService.getRunTimeAssetUtilization(param);
                List<RunTimeItemValue> list = (List<RunTimeItemValue>) reply.getData();
                List<RunTimeItemValue> lists = new ArrayList<>();
                if (param.getSearchName() != null && !param.getSearchName().trim().equals("")) {
                    for (RunTimeItemValue r : list) {
                        if (r.getAssetName().contains(param.getSearchName())) {
                            lists.add(r);
                        }
                    }
                } else {
                    lists.addAll(list);
                }
                lists = lists.subList(0, datesize == 0 || datesize > lists.size() ? lists.size() : datesize);
                return setResultSuccess(Reply.ok(lists));
            }
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("browse{}", e);
            return setResultFail(e.getMessage(), param);
        }

        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "report_manage")
    @PostMapping("/runtimeOfTopN/optimize/browse")
    @ResponseBody
    @ApiOperation(value = "运行状态报表topN查询2")
    public ResponseBase selectOptimizeRunTimeTopN(@RequestBody RunTimeQueryParam param) {
        Reply reply = null;
        try {
            /**
             * 查询运行状态报表topN所有数据
             */
            if (param.getDateType()==0){
                reply = mwreportService.getRunTimeItemOptimizeUtilization(param, false,true);
            }else{
                reply = mwreportService.getRunTimeItemOptimizeUtilization(param, false,false);
            }


        } catch (Throwable e) {
            logger.error("browse{}", e);
            return setResultFail(e.getMessage(), param);
        }

        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "report_manage")
    @PostMapping("/runtimeOfTrend/browse")
    @ResponseBody
    @ApiOperation(value = "运行状态报表周期趋势查询")
    public ResponseBase selectRunTimeTest3(@RequestBody RunTimeQueryParam param) {
        Reply reply;
        try {
            reply = mwreportService.getRunTimeReportTrend(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("browse{}", e);
            return setResultFail(e.getMessage(), param);
        }

        return setResultSuccess(reply);

    }

    @MwPermit(moduleName = "report_manage")
    @PostMapping("/runtimeOfTrend/optimize/browse")
    @ResponseBody
    @ApiOperation(value = "运行状态报表周期趋势查询2")
    public ResponseBase selectOptimizeRunTimeThend(@RequestBody RunTimeQueryParam param) {
        Reply reply;
        try {
            reply = mwreportService.getOptimizeRunTimeReportTrend(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("browse{}", e);
            return setResultFail(e.getMessage(), param);
        }

        return setResultSuccess(reply);

    }

    @MwPermit(moduleName = "report_manage")
    @PostMapping("/browse")
    @ResponseBody
    @ApiOperation(value = "报表查询")
    public ResponseBase selectReport(@RequestBody QueryReportParam queryReportParam) {
        Reply reply;
        try {
            reply = mwreportService.selectReport(queryReportParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("browse{}", e);
            return setResultFail(e.getMessage(), queryReportParam);
        }

        return setResultSuccess(reply);

    }

    @MwPermit(moduleName = "report_manage")
    @PostMapping("/popup/browse")
    @ResponseBody
    @ApiOperation(value = "查询报表详情")
    public ResponseBase getReportDetail(@RequestBody TrendParam trendParam) {
        Reply reply;
        try {
            if (trendParam.getParticle() != null) {
                reply = mwSyReportService.getReportDetail(trendParam);
            } else {
                reply = mwreportService.getReportDetail(trendParam);
            }
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("getReportDetail{}", e);
            return setResultFail(e.getMessage(), trendParam);
        }

        return setResultSuccess(reply);
    }

    @PostMapping("/reportDetailColumn/browse")
    @ResponseBody
    @ApiOperation(value = "查询下拉框选中数据")
    public ResponseBase getSelectColumnByType(@RequestParam int assetsTypeId) {

        Reply reply;
        try {
            reply = mwSyReportService.getSelectColumnByType(assetsTypeId);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Exception e) {
            return setResultFail(e.getMessage(), assetsTypeId);
        }
        return setResultSuccess(reply);

    }

    /**
     * 用户查询线路历史趋势
     */
    @MwPermit(moduleName = "report_manage")
    @PostMapping("/popup/perform")
    @ResponseBody
    public ResponseBase selectHistory(@RequestBody TrendParam trendParam) {
        Reply reply;
        try {
            reply = mwreportService.selectLinkHistory(trendParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("selectHistory{}", e);
            return setResultFail(e.getMessage(), "查询线路历史失败");
        }

        return setResultSuccess(reply);
    }

    /**
     * 根据interfaceIds 查询rootPort,targetPort
     */
    @MwPermit(moduleName = "report_manage")
    @PostMapping("/getLinkPerformDropdown/browse")
    @ResponseBody
    public ResponseBase selectLinkEditDropdown(@RequestBody TrendParam trendParam) {
        Reply reply;
        try {
            reply = mwreportService.selectLinkEditDropdown(trendParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("selectHistory{}", e);
            return setResultFail(e.getMessage(), "查询link线路折线图下拉数据失败");
        }

        return setResultSuccess(reply);
    }

    /**
     * 根据interfaceIds 获取所有的历史记录并展示出来
     */
    @MwPermit(moduleName = "report_manage")
    @PostMapping("/getHistoryByList/browse")
    @ResponseBody
    public ResponseBase getHistoryByList(@RequestBody TrendParam param) {
        Reply reply;
        try {
            reply = mwreportService.getHistoryByList(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getHistoryByList{}", e);
            return setResultFail(e.getMessage(), param);
        }

        return setResultSuccess(reply);
    }

    /**
     * 根据带宽利用率的10% 50% 80% 对比最大值和平均值来筛选数据
     */
    @MwPermit(moduleName = "report_manage")
    @PostMapping("/groupSelect/browse")
    @ResponseBody
    public ResponseBase groupSelect(@RequestBody TrendParam param) {
        Reply reply;
        try {
            reply = mwreportService.groupSelect(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("groupSelect{}", e);
            return setResultFail(e.getMessage(), param);
        }

        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "report_manage")
    @PostMapping("/link/export")
    @ResponseBody
    @ApiOperation(value = "线路流量导出")
    public void export(@RequestBody ExportLinkParam uParam, HttpServletResponse response) {
        try {
            mwreportService.exportLink(uParam, response);
        } catch (Throwable e) {
            log.error(e.getMessage());
        }
    }

    @MwPermit(moduleName = "report_manage")
    @PostMapping("/input/perform")
    @ResponseBody
    @ApiOperation(value = "导入数据库功能")
    public ResponseBase inputTime(@RequestBody EditorTimeParam inputParam) {
        Reply reply;
        try {
            reply = mwreportService.inputTime(inputParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("inputTime{}", e);
            return setResultFail(e.getMessage(), ErrorConstant.SOLAR_CARRIERNAME_SELECT_MSG_306002);
        }

        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "report_manage")
    @PostMapping("/export")
    @ResponseBody
    @ApiOperation(value = "报表excel导出")
    public void export(@RequestBody ExcelReportParam uParam, HttpServletResponse response) {
        try {
            if (null != uParam.getParticle()) {
                mwSyReportService.export(uParam, response);
            } else {
                mwreportService.export(uParam, response);
            }
        } catch (Throwable e) {
            log.error("export{}", e);
        }
    }


}
