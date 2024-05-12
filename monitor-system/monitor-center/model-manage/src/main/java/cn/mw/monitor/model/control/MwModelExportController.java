package cn.mw.monitor.model.control;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.model.param.ModelExportDataInfoListParam;
import cn.mw.monitor.model.param.ModelExportDataInfoParam;
import cn.mw.monitor.model.service.MwModelExportService;
import cn.mw.monitor.model.service.MwModelViewService;
import cn.mw.monitor.service.model.param.QueryInstanceModelParam;
import cn.mw.monitor.service.model.param.QueryModelInstanceParam;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mwpaas.common.model.Reply;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.TimeUnit;

/**
 * 模型管理系统 数据导入导出
 *
 * @author qzg
 * @date 2021/12/06
 */
@RequestMapping("/mwapi/model")
@Controller
@Slf4j
@Api(value = "模型管理数据导入导出", tags = "模型管理数据导入导出")
public class MwModelExportController extends BaseApiService {

    @Autowired
    private MwModelExportService mwModelExportService;

    @Autowired
    private MwModelViewService mwModelViewService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;

    //文件上传路径
    @Value("${file.url}")
    private String filePath;

    //logo上传目录
    static final String MODULE = "file-upload";

    //共享模型数据，流程数据
    /*    @MwPermit(moduleName = "model_manage")*/
    @PostMapping("/import/getAllModelList")
    @ResponseBody
    @ApiOperation(value = "获取所有模型数据")
    public ResponseBase getAllModelList() {
        Reply reply = new Reply();
        try {
            reply = mwModelExportService.getAllModelList();
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("getAllModelList{}", e);
            return setResultFail("获取所有模型数据失败", "");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/import/getFieldByFile")
    @ResponseBody
    @ApiOperation(value = "获取上传文件的表头字段")
    public ResponseBase getFieldByFile(MultipartHttpServletRequest request) {
        Reply reply = new Reply();
        try {
            MultipartFile file = request.getFile("file");
            String json = request.getParameter("importParam");
            ModelExportDataInfoParam param = JSONObject.parseObject(json,ModelExportDataInfoParam.class);
            reply = mwModelExportService.getFieldByFile(file, param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("getFieldByFile{}", e);
            return setResultFail("获取上传文件的表头字段失败", "");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/import/importDataInfo")
    @ResponseBody
    @ApiOperation(value = "上传文件数据导入")
    public ResponseBase importDataInfo(MultipartHttpServletRequest request) {
        Reply reply = new Reply();
        try {
            MultipartFile file = request.getFile("file");
            String json = request.getParameter("importParam");
            ModelExportDataInfoListParam param = JSONObject.parseObject(json,ModelExportDataInfoListParam.class);
            reply = mwModelExportService.exportDataInfo(file, param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("fail to importDataInfo{}", e);
            return setResultFail("数据导入失败", "");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/export/instanceListInfo")
    @ResponseBody
    @ApiOperation("数据导出Excel接口")
    public ResponseBase exportForExcel(@RequestBody QueryInstanceModelParam param, HttpServletRequest request, HttpServletResponse response) {
        Reply reply;
        try {
            param.setIsAssetsView(false);
            reply = mwModelViewService.exportForExcel(param, request, response);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("exportForExcel fail{}", e);
            return setResultFail("数据导出失败", "");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/export/template")
    @ResponseBody
    @ApiOperation("数据模板 导出")
    public ResponseBase exportTemplatel(@RequestBody QueryModelInstanceParam param, HttpServletRequest request, HttpServletResponse response) {
        Reply reply;
        try {
            reply = mwModelExportService.exportTemplatel(param, request, response);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("exportTemplatel fail{}", e);
            return setResultFail("数据导出失败", "");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/batchInsert/import")
    @ResponseBody
    @ApiOperation(value = "批量新增之数据导入")
    public ResponseBase batchInsertImportData(MultipartHttpServletRequest request) {
        Reply reply = new Reply();
        try {
            MultipartFile file = request.getFile("file");
            reply = mwModelExportService.batchInsertImportData(file);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("fail to importDataInfo{}", e);
            return setResultFail("数据导入失败", "");
        }
    }
}
