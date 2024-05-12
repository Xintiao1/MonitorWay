package cn.mw.monitor.assetsTemplate.control;

import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.assetsTemplate.api.param.assetsTemplate.QueryTemplateExportParam;
import cn.mw.monitor.assetsTemplate.service.MwAssetsTemplateExportService;
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

import javax.servlet.http.HttpServletResponse;

/**
 * @author qzg
 * @date 2021/6/24
 */
@RequestMapping("/mwapi/assetsTemplateExport")
@Controller
@Slf4j
@Api(value = "资产管理模板管理导出接口", tags = "资产管理模板管理导出接口")
public class MwAssetsTemplateExportController extends BaseApiService {
    private static final Logger logger = LoggerFactory.getLogger("MwAssetsTemplateExportController");

    @Autowired
    MwAssetsTemplateExportService mwAssetsTemplateExportService;

    @PostMapping("/templateInfoExport")
    @ResponseBody
    @ApiOperation("资产模板导出Excel接口")
    public ResponseBase templateInfoExport(@RequestBody QueryTemplateExportParam qParam, HttpServletResponse response) {
        Reply reply;
        try {
            reply = mwAssetsTemplateExportService.templateInfoExport(qParam, response);
            return setResultSuccess(reply);
        } catch (Exception e) {
            logger.error("数据导出失败", e);
            return setResultFail("数据导出失败", "");
        }
    }

    @PostMapping("/templateInfoImport")
    @ResponseBody
    @ApiOperation("资产模板导入数据接口")
    public ResponseBase templateInfoImport(@RequestBody MultipartFile file, HttpServletResponse response) {
        Reply reply;
        try {
            reply = mwAssetsTemplateExportService.templateInfoImport(file, response);
            return setResultSuccess(reply);
        } catch (Exception e) {
            logger.error("导入数据失败", e);
            return setResultFail("导入数据失败", "");
        }
    }
}
