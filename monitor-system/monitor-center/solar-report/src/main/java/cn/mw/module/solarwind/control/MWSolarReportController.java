package cn.mw.module.solarwind.control;

import cn.mw.module.solarwind.dto.InterfaceReportDto;
import cn.mw.module.solarwind.service.MWCreatePdf;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.module.solarwind.param.ExportSolarParam;
import cn.mw.module.solarwind.param.InputParam;
import cn.mw.module.solarwind.param.QueryParam;
import cn.mw.module.solarwind.service.MWSolarReportService;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xhy
 * @date 2020/6/22 15:11
 */
@RequestMapping("/mwapi/solarReport")
@Controller
@Slf4j
@ConditionalOnProperty(prefix = "solarwind", name = "enable", havingValue = "true")
@Api(value = "solar报表", tags = "solar报表")
public class MWSolarReportController extends BaseApiService implements InitializingBean {

    @Autowired(required = false)
    private MWSolarReportService mwSolarReportService;

    //文件上传路径
    @Value("${file.url}")
    private String filePath;

    /**
     * 下拉列表
     * 查询interface的carrierName
     */
    @PostMapping("/selectCarrierName")
    @ResponseBody
    public ResponseBase selectCarrierName() {
        Reply reply;
        try {
            reply = mwSolarReportService.selectCarrierName();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(ErrorConstant.SOLAR_CARRIERNAME_SELECT_MSG_306002,"");
        }

        return setResultSuccess(reply);
    }

    /**
     * 下拉列表
     * 查询interface的caption
     */
    @PostMapping("/selectCaption")
    @ResponseBody
    public ResponseBase selectCaption(@RequestBody QueryParam param) {
        Reply reply;
        try {
            reply = mwSolarReportService.selectCaption(param.getCarrierName());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(ErrorConstant.SOLAR_CARRIERNAME_SELECT_MSG_306002,"");
        }

        return setResultSuccess(reply);
    }

    /**
     * 根据带宽利用率的10% 50% 80% 对比最大值和平均值来筛选数据
     */
    @PostMapping("/groupSelect/browse")
    @ResponseBody
    public ResponseBase groupSelect(@RequestBody QueryParam param) {
        Reply reply;
        try {
            reply = mwSolarReportService.groupSelect(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("MWSolarReportController{} groupSelect() error","");
        }

        return setResultSuccess(reply);
    }


    /**
     * 分类查询报表
     */
    @PostMapping("/browse")
    @ResponseBody
    public ResponseBase selectSolarReportList(@RequestBody QueryParam param, HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwSolarReportService.selectSolarReportList(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("MWSolarReportController{} selectSolarReportList() error","");
        }

        return setResultSuccess(reply);
    }


    /**
     * 用户查询线路历史趋势
     */
    @PostMapping("/popup/perform")
    @ResponseBody
    public ResponseBase selectHistory(@RequestBody QueryParam queryParam, HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwSolarReportService.selectHistory(queryParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(ErrorConstant.SOLAR_TIME_SELECT_MSG_306004,"");
        }

        return setResultSuccess(reply);
    }

    /**
     * 根据interfaceIds 获取所有的历史记录并展示出来
     */
    @PostMapping("/getHistoryByList/browse")
    @ResponseBody
    public ResponseBase getHistoryByList(@RequestBody QueryParam param) {
        Reply reply;
        try {
            reply = mwSolarReportService.getHistoryByList(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("MWSolarReportController{} getHistoryByList() error","");
        }

        return setResultSuccess(reply);
    }

    @PostMapping("/perform")
    @ResponseBody
    public ResponseBase inputSolarTime(@RequestBody InputParam inputParam) {
        Reply reply;
        try {
            reply = mwSolarReportService.inputSolarTime(inputParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(ErrorConstant.SOLAR_CARRIERNAME_SELECT_MSG_306002,"");
        }

        return setResultSuccess(reply);
    }


    @PostMapping("/export")
    @ResponseBody
    @ApiOperation(value = "线路流量导出")
    public void export(@RequestBody ExportSolarParam uParam, HttpServletResponse response) {
        try {
            mwSolarReportService.export(uParam, response);
        } catch (Throwable e) {
            log.error(e.getMessage());
        }
    }

    @PostMapping("/selectAll")
    @ResponseBody
    @ApiOperation(value = "查詢所有")
    public ResponseBase selectAll() {
         mwSolarReportService.selectAll();
        return setResultSuccess("ok");

    }

    /**
     * 导出PDF文件
     * @param param 查询参数
     * @param response
     */
    @PostMapping("/exportpdf")
    @ResponseBody
    @ApiOperation(value = "线路流量导出PDF")
    public void exportPdf(@RequestBody QueryParam param, HttpServletResponse response) {
        try {
            mwSolarReportService.exportPdf(param, response,filePath);
        } catch (Throwable e) {
            log.error(e.getMessage());
        }
    }




//    @MwPermit(moduleName = "report_manage")
//    @GetMapping("/exportpdf")
//    @ApiOperation(value = "资产可用性报表导出")
//    public void exportPdf(HttpServletResponse response) {
//        try {
//            QueryParam param = new QueryParam();
//            param.setDayType(0);
//            param.setInterfaceID(0);
//            param.setIsExport(false);
//            param.setPeriodRadio("WORKDAY");
//            param.setSeniorchecked(false);
//            param.setValueType("AVG");
////            mwSolarReportService.exportPdf(param, response);
//            List<InterfaceReportDto> list = new ArrayList<>();
//            InterfaceReportDto dto1 = new InterfaceReportDto();
//            dto1.setCaption("线路1");
//            dto1.setInBandwidth("带宽1");
//            InterfaceReportDto dto2 = new InterfaceReportDto();
//            dto2.setCaption("线路1");
//            dto2.setInBandwidth("带宽1");
//            list.add(dto1);
//            list.add(dto2);
//            //进行数据导出
//            String[] head = {"线路名称","带宽","接入流量(入向)最大","接入流量(入向)最小","接入流量(入向)平均","接入流量(入向)最大利用率","接入流量(入向)平均利用率","接口流量时间占比(入向)<10%",  "接口流量时间占比(入向)10%-50%","接口流量时间占比(入向)50%-80%","接口流量时间占比(入向)>80%","接出流量(出向)最大","接出流量(出向)最小","接出流量(出向)平均",  "接出流量(出向)最大利用率","接出流量(出向)平均利用率","接口流量时间占比(出向)<10%","接口流量时间占比(出向)10%-50%","接口流量时间占比(出向)50%-80%","接口流量时间占比(出向)>80%"};
//            new MWCreatePdf().generatePDFs(head,list,response,filePath);
//        } catch (Throwable e) {
//            log.error(e.getMessage());
//        }
//    }


    @Override
    public void afterPropertiesSet() throws Exception {
        log.info(">>>>>>>MWSolarReportController >>>>>>>>>>");
    }
}
