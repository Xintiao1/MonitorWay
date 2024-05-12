package cn.mw.monitor.api.controller;

import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.templatemanage.entity.AddTemplateManageParam;
import cn.mw.monitor.templatemanage.entity.ParamEntity;
import cn.mw.monitor.templatemanage.entity.QueryTemplateManageParam;
import cn.mw.monitor.templatemanage.service.MwTemplateManageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RequestMapping("/mwapi")
@Controller
@Slf4j
@Api(value = "模板管理")
public class MWTemplateManageController extends BaseApiService {

    @Autowired
    private MwTemplateManageService mwTemplateManageService;

    @PostMapping("/templateManage/editorBrowse")
    @ResponseBody
    @ApiOperation(value = "模板管理编辑前查询")
    public ResponseBase editorBrowse(@RequestBody QueryTemplateManageParam param) {
        Reply reply;
        try {
            reply = mwTemplateManageService.selectList1(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            return setResultFail("模板管理编辑前查询失败", "");
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/templateManage/browse")
    @ResponseBody
    @ApiOperation(value = "模板管理查询")
    public ResponseBase selectReport(@RequestBody QueryTemplateManageParam param) {
        Reply reply;
        try {
            reply = mwTemplateManageService.selectList(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            return setResultFail("模板管理查询失败", "");
        }
        return setResultSuccess(reply);
    }

    @RequestMapping(value = "/templateManage/dropDownBrowse",method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    @ApiOperation(value = "模板管理查询")
    public ResponseBase selectReport() {
        Reply reply;
        try {
            reply = mwTemplateManageService.selectListDropDown();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            return setResultFail("模板管理查询失败", "");
        }
        return setResultSuccess(reply);
    }



    @PostMapping("/templateManage/delete")
    @ResponseBody
    @ApiOperation(value = "模板管理删除")
    public ResponseBase delete(@RequestBody List<Integer> list) {
        Reply reply;
        try {
            reply = mwTemplateManageService.delete(list);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("模板管理删除失败", "");
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/templateManage/editor")
    @ResponseBody
    @ApiOperation(value = "模板管理修改")
    public ResponseBase update(@Validated @RequestBody AddTemplateManageParam param) {
        Reply reply;
        try {
            reply = mwTemplateManageService.update(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("模板管理修改失败", "");
        }
        return setResultSuccess(reply);
    }

    @ApiOperation(value = "新增模板管理")
    @PostMapping("/templateManage/create")
    @ResponseBody
    public ResponseBase add(@Validated  @RequestBody AddTemplateManageParam param,
                            HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwTemplateManageService.insert(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("新增模板管理失败", "");
        }
        return setResultSuccess(reply);
    }

    @ApiOperation(value = "查询品牌")
    @PostMapping("/templateManage/brand")
    @ResponseBody
    public ResponseBase brand(@RequestBody ParamEntity param,
                            HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwTemplateManageService.brand(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("查询品牌失败", "");
        }
        return setResultSuccess(reply);
    }

    @ApiOperation(value = "查询规格型号")
    @PostMapping("/templateManage/specification")
    @ResponseBody
    public ResponseBase specification(@RequestBody ParamEntity param,
                              HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwTemplateManageService.specification(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("查询规格型号失败", "");
        }
        return setResultSuccess(reply);
    }



}
