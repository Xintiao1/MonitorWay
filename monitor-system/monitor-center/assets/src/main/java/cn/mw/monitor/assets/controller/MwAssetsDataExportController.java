package cn.mw.monitor.assets.controller;

import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.assets.dto.MwAssetsDataExportDto;
import cn.mw.monitor.assets.service.MwAssetsDataExportService;
import cn.mw.monitor.assets.service.MwIntangibleAssetsService;
import cn.mwpaas.common.model.Reply;
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
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author qzg
 * @date 2021/6/24
 */
@RequestMapping("/mwapi/assetsDataExport")
@Controller
@Slf4j
@Api(value = "资产管理数据导出接口", tags = "资产管理数据导出接口")
public class MwAssetsDataExportController extends BaseApiService {
    @Autowired
    private MwAssetsDataExportService mwAssetsDataExportService;

    @PostMapping("/exportForExcel/perform")
    @ResponseBody
    @ApiOperation("数据导出Excel接口")
    public ResponseBase exportForExcel(@RequestBody MwAssetsDataExportDto param, HttpServletRequest request, HttpServletResponse response) {
        Reply reply;
        try {
            reply = mwAssetsDataExportService.exportForExcel(param, request, response);

            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("数据导出失败", e);
            return setResultFail("数据导出失败", "");
        }
    }


    @PostMapping("/exportLayout/perform")
    @ResponseBody
    @ApiOperation("布局数据导出Excel接口")
    public ResponseBase exportComponentLayoutForExcel(@RequestBody MwAssetsDataExportDto param, HttpServletRequest request, HttpServletResponse response) {
        Reply reply;
        try {
            reply = mwAssetsDataExportService.exportComponentLayoutForExcel(param, request, response);

            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("布局数据导出Excel接口失败", e);
            return setResultFail("布局数据导出Excel接口失败", "");
        }
    }


    @PostMapping("/importLayout/perform")
    @ResponseBody
    @ApiOperation("布局数据导入Excel接口")
    public ResponseBase importComponentLayoutForExcel(@RequestBody MultipartFile file, HttpServletResponse response) {
        Reply reply;
        try {
            reply = mwAssetsDataExportService.importComponentLayoutForExcel(file, response);

            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("布局数据导入Excel接口失败", e);
            return setResultFail("布局数据导入Excel接口失败", "");
        }
    }

    @PostMapping("/exportMissTemp/perform")
    @ResponseBody
    @ApiOperation("布局模板缺失数据导出")
    public ResponseBase missTypeExport(HttpServletResponse response) {
        Reply reply;
        try {
            reply = mwAssetsDataExportService.missTypeExport(response);

            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("布局模板缺失数据导出失败", e);
            return setResultFail("布局模板缺失数据导出失败", "");
        }
    }

    @PostMapping("/isCoverImportData/perform")
    @ResponseBody
    @ApiOperation("是否覆盖导入数据")
    public ResponseBase isCoverImportData() {
        Reply reply;
        try {
            reply = mwAssetsDataExportService.isCoverImportData();

            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("覆盖导入数据失败", e);
            return setResultFail("覆盖导入数据失败", "");
        }
    }

    @PostMapping("/exportIndex")
    @ResponseBody
    @ApiOperation("导出资产监控项指标")
    public void exportAssetsIndex(HttpServletResponse response) {
        try {
            mwAssetsDataExportService.exportAssetsIndex(response);
        } catch (Exception e) {
            log.error("导出资产监控项指标失败", e);
        }
    }
}
