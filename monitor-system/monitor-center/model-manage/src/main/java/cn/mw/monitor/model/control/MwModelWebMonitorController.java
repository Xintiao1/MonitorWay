package cn.mw.monitor.model.control;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.bean.ExcelExportParam;
import cn.mw.monitor.model.dto.MwModelWEBProxyDTO;
import cn.mw.monitor.model.param.AddAndUpdateModelWebMonitorParam;
import cn.mw.monitor.model.param.MwModelUpdateWebMonitorStateParam;
import cn.mw.monitor.model.param.MwModelWebMonitorTable;
import cn.mw.monitor.model.service.MwModelWebMonitorService;
import cn.mw.monitor.service.server.api.MwServerService;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.PathParam;
import java.util.List;

/**
 * @author qzg
 * @date 2023/5/20 8:58
 */
@RequestMapping("/mwapi/modelWeb")
@Controller
@Slf4j
@Api(value = "模型web监测接口", tags = "模型web监测接口")
public class MwModelWebMonitorController extends BaseApiService {

    @Autowired
    private MwModelWebMonitorService mwModelWebMonitorService;
    @Autowired
    private MwServerService service;


    @MwPermit(moduleName = "model_manage")
    @PostMapping("/webMonitorServer/browse")
    @ResponseBody
    @ApiOperation(value = "web监测服务器查询")
    public ResponseBase selectWebSeverInfo(@RequestBody MwModelWEBProxyDTO param) {
        Reply reply;
        try {
            reply = mwModelWebMonitorService.selectWebSeverInfo(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("selectWebSeverInfo{}", e);
            return setResultFail("web监测服务器查询失败", "");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/webSever/create")
    @ResponseBody
    @ApiOperation(value = "web监测新建")
    public ResponseBase createWebSeverData(@RequestBody @Validated AddAndUpdateModelWebMonitorParam param) {
        Reply reply = null;
        try {
            mwModelWebMonitorService.createWebSeverData(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("selectWebSeverInfo{}", e);
            return setResultFail("web监测新建失败", "");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/webSeverList/browse")
    @ResponseBody
    @ApiOperation(value = "web监测信息查询")
    public ResponseBase queryWebSeverList(@RequestBody List<MwModelWebMonitorTable> params) {
        Reply reply;
        try {
            mwModelWebMonitorService.queryWebSeverList(params);
            return setResultSuccess();
        } catch (Exception e) {
            log.error("selectWebSeverInfo{}", e);
            return setResultFail("web监测信息查询失败", "");
        }
    }

    /**
     * 导出web监测模板
     *
     * @param response
     */
    @MwPermit(moduleName = "model_manage")
    @PostMapping("/webMonitor/excelTemplate")
    @ResponseBody
    public void excelTemplateExport(@RequestBody ExcelExportParam excelExportParam, HttpServletResponse response) {
        try {
            mwModelWebMonitorService.excelTemplateExport(excelExportParam, response);
        } catch (Exception e) {
            log.error("excelTemplateExport{}", e);
        }
    }

    /**
     * 批量导入web监测
     *
     * @param file
     */
    @MwPermit(moduleName = "model_manage")
    @PostMapping("/webMonitor/excelImport")
    @ResponseBody
    public void excelImportWebMonitor(@RequestBody MultipartFile file, HttpServletResponse response) {
        try {
             mwModelWebMonitorService.excelImportWebMonitor(file,response);
        } catch (Exception e) {
            log.error("数据导入失败", e);
        }
    }

    @MwPermit(moduleName = "model_manage")
    @GetMapping("/getSysByhostId/browse")
    @ResponseBody
    @ApiOperation(value = "获得当前资产主键Id档案")
    public ResponseBase getSysByhostId(@PathParam("id") String id, @PathParam("moduleType") String moduleType) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = service.getRecordByAssetsId(id, moduleType);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("");
            return setResultFail("获得当前资产档案信息失败", "");
        }

        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/updateState/editor")
    @ResponseBody
    @ApiOperation(value = "web监测修改启用状态")
    public ResponseBase updateState(@RequestBody MwModelUpdateWebMonitorStateParam updateWebMonitorStateParam) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwModelWebMonitorService.updateState(updateWebMonitorStateParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("");
            return setResultFail("web监测修改启用状态失败", "");
        }

        return setResultSuccess(reply);
    }
}
