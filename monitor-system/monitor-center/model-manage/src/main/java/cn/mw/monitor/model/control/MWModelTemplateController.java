package cn.mw.monitor.model.control;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.model.param.*;
import cn.mw.monitor.model.service.MWModelTemplateService;
import cn.mw.monitor.service.assets.param.AssetsSearchTermFuzzyParam;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author qzg
 * @date 2022/5/05
 */
@RequestMapping("/mwapi/modelTemplate")
@Controller
@Slf4j
@Api(value = "资源中心", tags = "模板管理接口")
public class MWModelTemplateController extends BaseApiService {
    @Autowired
    private MWModelTemplateService mwModelTemplateService;

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/template/create")
    @ResponseBody
    @ApiOperation(value = "模板新建接口")
    public ResponseBase addModelTemplate(@RequestBody AddAndUpdateModelTemplateParam param) {
        Reply reply = new Reply();
        try {
            reply = mwModelTemplateService.addModelTemplate(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("addModelTemplate{}", e);
            return setResultFail("模板新建失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/template/editor")
    @ResponseBody
    @ApiOperation(value = "模板修改接口")
    public ResponseBase updateModelTemplate(@RequestBody AddAndUpdateModelTemplateParam qParam) {
        Reply reply = new Reply();
        try {
            reply = mwModelTemplateService.updateModelTemplate(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("updateModelTemplate{}", e);
            return setResultFail("模板修改失败", "");
        }
    }


    @MwPermit(moduleName = "model_manage")
    @PostMapping("/template/browse")
    @ResponseBody
    @ApiOperation(value = "模板查询接口")
    public ResponseBase queryModelTemplate(@RequestBody QueryModelTemplateParam param) {
        Reply reply = new Reply();
        try {
            reply = mwModelTemplateService.queryModelTemplate(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("queryModelTemplate{}", e);
            return setResultFail("模板查询失败", "");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/template/popup/browse")
    @ResponseBody
    @ApiOperation(value = "模板编辑查询接口")
    public ResponseBase popupBrowseModelTemplate(@RequestBody QueryModelTemplateParam param) {
        Reply reply = new Reply();
        try {
            reply = mwModelTemplateService.popupBrowseModelTemplate(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("popupBrowseModelTemplate{}", e);
            return setResultFail("模板编辑查询失败", "");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/template/delete")
    @ResponseBody
    @ApiOperation(value = "模板删除接口")
    public ResponseBase deleteModelTemplate(@RequestBody AddAndUpdateModelTemplateParam qParam) {
        Reply reply = new Reply();
        try {
            reply = mwModelTemplateService.deleteModelTemplate(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("deleteModelTemplate{}", e);
            return setResultFail("模板删除失败", "");
        }
    }

    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/template/fuzzSearch/browse")
    @ResponseBody
    public ResponseBase fuzzSearchAllFiledData(@RequestBody ModelTemplateSearchFuzzyParam param) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwModelTemplateService.fuzzSearchAllFiledData(param.getValue());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("");
            return setResultFail("模糊查询数据失败", "");
        }

        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "model_manage")
    @PostMapping("/template/getzbxTemplate")
    @ResponseBody
    @ApiOperation(value = "zabbix所有模板下拉框查询")
    public ResponseBase templateGet() {
        Reply reply = new Reply();
        try {
            reply = mwModelTemplateService.templateGet();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("templateGet{}", e);
            return setResultFail("zabbix所有模板下拉框查询失败", "");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/updateAssetsTemplate/editor")
    @ResponseBody
    @ApiOperation(value = "关联template_id批量更新")
    public ResponseBase updateModelTemplateByMore() {
        Reply reply = new Reply();
        try {
            reply = mwModelTemplateService.updateModelTemplateByMore();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("updateModelTemplateByMore{}", e);
            return setResultFail("批量更新失败", "");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/template/export")
    @ResponseBody
    @ApiOperation("资产模板导出Excel接口")
    public ResponseBase templateInfoExport(@RequestBody QueryModelTemplateExportParam qParam, HttpServletResponse response) {
        Reply reply;
        try {
            reply = mwModelTemplateService.templateInfoExport(qParam, response);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("templateInfoExport{}", e);
            return setResultFail("数据导出失败", "");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/template/import")
    @ResponseBody
    @ApiOperation("资产模板导入数据接口")
    public ResponseBase templateInfoImport(@RequestBody MultipartFile file, HttpServletResponse response) {
        Reply reply;
        try {
            reply = mwModelTemplateService.templateInfoImport(file, response);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("templateInfoImport{}", e);
            return setResultFail("导入数据失败", "");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/template/exportByFormwork")
    @ResponseBody
    @ApiOperation("模板导出")
    public ResponseBase exportByFormwork(HttpServletRequest request, HttpServletResponse response) {
        Reply reply;
        try {
            reply = mwModelTemplateService.exportByFormwork(request, response);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("数据导出失败", e);
            return setResultFail("数据导出失败", "");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("templateStatus/editor")
    @ResponseBody
    @ApiOperation("修改模板可用状态")
    public ResponseBase updateTemplateStatus(@RequestBody List<UpdateModelTemplateStatusParam> list) {
        Reply reply;
        try {
            reply = mwModelTemplateService.updateTemplateStatus(list);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("修改模板可用状态失败", e);
            return setResultFail("修改模板可用状态失败", "");
        }
    }
}
