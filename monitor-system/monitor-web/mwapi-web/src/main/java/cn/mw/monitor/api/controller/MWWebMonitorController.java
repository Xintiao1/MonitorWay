package cn.mw.monitor.api.controller;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.bean.ExcelExportParam;
import cn.mw.monitor.service.assets.model.MwCommonAssetsDto;
import cn.mw.monitor.service.license.service.CheckCountService;
import cn.mw.monitor.service.license.service.LicenseManagementService;
import cn.mw.monitor.webMonitor.api.param.webMonitor.AddUpdateWebMonitorParam;
import cn.mw.monitor.webMonitor.api.param.webMonitor.BatchUpdateParam;
import cn.mw.monitor.webMonitor.api.param.webMonitor.DeleteWebMonitorParam;
import cn.mw.monitor.webMonitor.api.param.webMonitor.QueryWebHistoryParam;
import cn.mw.monitor.webMonitor.api.param.webMonitor.QueryWebMonitorParam;
import cn.mw.monitor.webMonitor.api.param.webMonitor.UpdateWebMonitorStateParam;
import cn.mw.monitor.webMonitor.service.MwWebMonitorService;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
 * web监测api管理
 * @auth baochengbin
 * @desc
 * @date 2020/3/16
 */
@Api(tags = "web监测api管理")
@RequestMapping("/mwapi")
@Controller
@Slf4j
public class MWWebMonitorController extends BaseApiService {
    private static final Logger logger = LoggerFactory.getLogger("control-" + MWWebMonitorController.class.getName());

    @Autowired
    MwWebMonitorService mwWebMonitorService;

    @Autowired
    LicenseManagementService licenseManagement;

    @Autowired
    CheckCountService checkCountService;

    /**
     * web监测修改
     */
    @MwPermit(moduleName = "assets_manage_web")
    @PostMapping("/getAssetsListByAssetsTypeId")
    @ResponseBody
    @ApiOperation("web监测修改")
    public ResponseBase getAssetsListByAssetsTypeId(@RequestBody MwCommonAssetsDto mwCommonAssetsDto){
        Reply reply;
        try{
            // 验证内容正确性
            reply = mwWebMonitorService.getAssetsListByAssetsTypeId(mwCommonAssetsDto);
            if(null != reply && reply.getRes() != PaasConstant.RES_SUCCESS){
                return setResultFail(reply.getMsg(), reply.getData());
            }
        }catch (Throwable e){
            logger.error("getAssetsListByAssetsTypeId{}",e);
            return setResultFail(e.getMessage(), mwCommonAssetsDto);
        }

        return setResultSuccess(reply);
    }

    /**
     * web监测新增
     */
    @MwPermit(moduleName = "assets_manage_web")
    @PostMapping("/webMonitor/create")
    @ResponseBody
    @ApiOperation("web监测新增")
    public ResponseBase addWebMonitor(@RequestBody @Validated AddUpdateWebMonitorParam addUpdateWebMonitorParam,
                                      HttpServletRequest request, RedirectAttributesModelMap model){
        Reply reply;
        try{
            //许可校验
            //数量获取
            int count = checkCountService.selectTableCount("mw_webmonitor_table", false);
            ResponseBase responseBase = licenseManagement.getLicenseManagemengt("assets_manage_web", count, 1);
            if (responseBase.getRtnCode() != 200) {
                return  setResultFail(responseBase.getMsg(), responseBase.getData());
            }

            reply = mwWebMonitorService.insertWebMonitor(addUpdateWebMonitorParam);
            if(null != reply && reply.getRes() != PaasConstant.RES_SUCCESS){
                return setResultFail(reply.getMsg(), reply.getData());
            }
        }catch (Throwable e){
            logger.error("addWebMonitor{}",e);
            return setResultFail(e.getMessage(), addUpdateWebMonitorParam);
        }

        return setResultSuccess(reply);
    }

    /**
     * web监测修改
     */
    @MwPermit(moduleName = "assets_manage_web")
    @PostMapping("/webMonitor/editor")
    @ResponseBody
    public ResponseBase updateWebMonitor(@RequestBody @Validated BatchUpdateParam updateWebMonitorParam,
                                      HttpServletRequest request, RedirectAttributesModelMap model){
        Reply reply;
        try{
            // 验证内容正确性
             reply = mwWebMonitorService.updateWebMonitor(updateWebMonitorParam);
            if(null != reply && reply.getRes() != PaasConstant.RES_SUCCESS){
                return setResultFail(reply.getMsg(), reply.getData());
            }
        }catch (Throwable e){
            logger.error("updateWebMonitor{}",e);
            return setResultFail(e.getMessage(), updateWebMonitorParam);
        }

        return setResultSuccess(reply);
    }

    /**
     * web监测删除
     */
    @MwPermit(moduleName = "assets_manage_web")
    @PostMapping("/webMonitor/delete")
    @ResponseBody
    public ResponseBase deleteWebMonitor(@RequestBody DeleteWebMonitorParam deleteWebMonitorParam,
                                         HttpServletRequest request, RedirectAttributesModelMap model){
        Reply reply;
        try{
            // 验证内容正确性
            reply = mwWebMonitorService.deleteWebMonitor(deleteWebMonitorParam);
            if(null != reply && reply.getRes() != PaasConstant.RES_SUCCESS){
                return setResultFail(reply.getMsg(), reply.getData());
            }
        }catch (Throwable e){
            logger.error("{}",e);
            return setResultFail(e.getMessage(), deleteWebMonitorParam);
        }

        return setResultSuccess(reply);
    }

    /**
     * web监测查询
     */
    @MwPermit(moduleName = "assets_manage_web")
    @PostMapping("/webMonitor/browse")
    @ResponseBody
    public ResponseBase browseWebMonitor(@RequestBody QueryWebMonitorParam browseWebMonitorParam,
                                         HttpServletRequest request, RedirectAttributesModelMap model){
        Reply reply;
        try{
            // 验证内容正确性
            reply = mwWebMonitorService.selectList(browseWebMonitorParam);
            if(null != reply && reply.getRes() != PaasConstant.RES_SUCCESS){
                return setResultFail(reply.getMsg(), reply.getData());
            }
        }catch (Throwable e){
            logger.error("browseWebMonitor{}",e);
            return setResultFail(e.getMessage(), browseWebMonitorParam);
        }

        return setResultSuccess(reply);
    }

    /**
     * web监测查询
     */
    @MwPermit(moduleName = "assets_manage_web")
    @PostMapping("/webMonitor/popup/browse")
    @ResponseBody
    public ResponseBase browsePopupWebMonitor(@RequestBody QueryWebMonitorParam browseWebMonitorParam,
                                         HttpServletRequest request, RedirectAttributesModelMap model){
        Reply reply;
        try{
            // 验证内容正确性
            reply = mwWebMonitorService.selectById(browseWebMonitorParam.getId());
            if(null != reply && reply.getRes() != PaasConstant.RES_SUCCESS){
                return setResultFail(reply.getMsg(), reply.getData());
            }
        }catch (Throwable e){
            logger.error("browsePopupWebMonitor{}",e);
            return setResultFail(e.getMessage(), browseWebMonitorParam);
        }

        return setResultSuccess(reply);
    }

    /**
     * WEB监测状态修改
     */
    @MwPermit(moduleName = "assets_manage_web")
    @PostMapping("webMonitor/perform")
    @ResponseBody
    public ResponseBase updateTangibleStatue(@RequestBody UpdateWebMonitorStateParam updateWebMonitorStateParam,
                                             HttpServletRequest request, RedirectAttributesModelMap model) {

        Reply reply;
        try {
             reply = mwWebMonitorService.updateState(updateWebMonitorStateParam);
            if (reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("updateTangibleStatue{}",e);
            return setResultFail(e.getMessage(), updateWebMonitorStateParam);
        }

        return setResultSuccess(reply);
    }

    /**
     * 历史查询
     */
    @MwPermit(moduleName = "assets_manage_web")
    @ApiOperation("查询Web监控历史信息")
    @PostMapping("/webMonitor/webMonitorHistory/browse")
    @ResponseBody
    public ResponseBase selectWebInfo(
            @ApiParam(value = "web历史信息拆查询参数",required = true)
            @RequestBody QueryWebHistoryParam queryWebHistoryParam,
                                      HttpServletRequest request, RedirectAttributesModelMap model){
        Reply reply;
        try{
            // 验证内容正确性
            reply = mwWebMonitorService.selectWebInfo(queryWebHistoryParam);
            if(null != reply && reply.getRes() != PaasConstant.RES_SUCCESS){
                return setResultFail(reply.getMsg(), reply.getData());
            }
        }catch (Throwable e){
            logger.error("selectWebInfo{}",e);
            return setResultFail(e.getMessage(), queryWebHistoryParam);
        }

        return setResultSuccess(reply);
    }

    /**
     * 批量导入web监测
     * @param file
     * @param response
     */
    @MwPermit(moduleName = "assets_manage_web")
    @PostMapping("/webMonitor/excelImport")
    @ResponseBody
    public void excelImport(@RequestBody MultipartFile file, HttpServletResponse response) {
        try {
            mwWebMonitorService.excelImport(file, response);
        } catch (Exception e) {
            logger.error("数据导入失败", e);
        }
    }

    /**
     * 导出web监测模板
     * @param response
     */
    @MwPermit(moduleName = "assets_manage_web")
    @PostMapping("/webMonitor/excelTemplate")
    @ResponseBody
    public void excelTemplateExport(@RequestBody ExcelExportParam excelExportParam, HttpServletResponse response) {
        try {
            mwWebMonitorService.excelTemplateExport(excelExportParam,response);
        } catch (Exception e) {
            logger.error("excelTemplateExport{}", e);
        }
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/webMonitor/fuzzSearchAllFiled/browse")
    @ResponseBody
    public ResponseBase fuzzSearchAllFiledData(@RequestBody QueryWebMonitorParam param) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwWebMonitorService.fuzzSearchAllFiledData(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(e.getMessage(), "模糊查询所有字段资数据失败");
        }

        return setResultSuccess(reply);
    }

}
