package cn.mw.monitor.api.controller;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.assetsSubType.service.MwAssetsSubTypeService;
import cn.mw.monitor.assetsTemplate.api.param.assetsTemplate.AddAssetsTemplateParam;
import cn.mw.monitor.assetsTemplate.api.param.assetsTemplate.DeleteAssetsTemplateParam;
import cn.mw.monitor.assetsTemplate.api.param.assetsTemplate.QueryAssetsTemplateParam;
import cn.mw.monitor.assetsTemplate.service.MwAssetsTemplateService;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.service.assets.param.AssetsSearchTermFuzzyParam;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author baochengbin
 * @date 2020/3/17
 */

@RequestMapping("/mwapi")
@Controller
@Slf4j
public class MWAssetsTemplateController extends BaseApiService {

    @Autowired
    MwAssetsSubTypeService mwAssetsSubTypeService;

    @Autowired
    MwAssetsTemplateService mwAssetsTemplateService;

    /**
     * 新增模板
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assetsTemplate/create")
    @ResponseBody
    public ResponseBase addEngineMange(@RequestBody AddAssetsTemplateParam aParam,
                                       HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwAssetsTemplateService.insert(aParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("addEngineMange", e);
            return setResultFail("MWAssetsTemplateController{} addEngineMange() error", "");
        }

        return setResultSuccess(reply);
    }

    /**
     * 删除模板
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assetsTemplate/delete")
    @ResponseBody
    public ResponseBase deleteEnigneManage(@RequestBody DeleteAssetsTemplateParam dParam,
                                           HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            List<Integer> idList = dParam.getIdList();
            // 验证内容正确性
            reply = mwAssetsTemplateService.delete(idList);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("deleteEnigneManage", e);
            return setResultFail("MWAssetsTemplateController{} deleteEnigneManage() error", "");
        }

        return setResultSuccess(reply);
    }

    /**
     * 修改模板
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assetsTemplate/editor")
    @ResponseBody
    public ResponseBase updateEngineMange(@RequestBody AddAssetsTemplateParam auParam,
                                          HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwAssetsTemplateService.update(auParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                if (reply.getRes().equals(ErrorConstant.ASSETSTEMPLATECODE_280102)) {
                    return setResult(ErrorConstant.ASSETSTEMPLATECODE_280102, reply.getMsg(), auParam);
                }
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("updateEngineMange", e);
            return setResultFail("MWAssetsTemplateController{} updateEngineMange() error", "");
        }
        return setResultSuccess(reply);
    }

    /**
     * 分页查询模板
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assetsTemplate/browse")
    @ResponseBody
    public ResponseBase browseEngineMange(@RequestBody QueryAssetsTemplateParam qParam,
                                          HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwAssetsTemplateService.selectTepmplateTableList(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("browseEngineMange", e);
            return setResultFail("MWAssetsTemplateController{} browseEngineMange() error", "");
        }

        return setResultSuccess(reply);
    }

    /**
     * 模板子页面查询
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assetsTemplate/popup/browse")
    @ResponseBody
    public ResponseBase assetsTemplatePopupBrowse(@RequestBody QueryAssetsTemplateParam qParam,
                                                  HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwAssetsTemplateService.selectById(qParam.getId());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("assetsTemplatePopupBrowse", e);
            return setResultFail("MWAssetsTemplateController{} assetsTemplatePopupBrowse() error", "");
        }

        return setResultSuccess(reply);
    }

    /**
     * zabbix所有模板下拉框查询
     */
    @MwPermit(moduleName = "assets_manage")
    @GetMapping("/assetsTemplate/zbxTemplate/browse")
    @ResponseBody
    public ResponseBase templateGet(@Param("name") String name,
                                    HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwAssetsTemplateService.templateGet(name);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("templateGet", e);
            return setResultFail("MWAssetsTemplateController{} templateGet() error", "");
        }

        return setResultSuccess(reply);
    }

//    /**
//     * 模板子页面查询
//     */
//    @PostMapping("/assetsTemplate/reset")
//    @ResponseBody
//    public ResponseBase resetTemplateIdBatch(HttpServletRequest request, RedirectAttributesModelMap model){
//        Reply reply;
//        try{
//            // 验证内容正确性
//            reply = mwAssetsTemplateService.resetTemplateIdBatch();
//            if(null != reply && reply.getRes() != PaasConstant.RES_SUCCESS){
//                return setResultFail(reply.getMsg(), reply.getData());
//            }
//        }catch (Throwable e){
//            logger.error(e.getMessage());
//            return setResultFail(e.getMessage(), null);
//        }
//
//        return setResultSuccess(reply);
//    }

    /**
     * 关联template_id批量更新
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/updateAssetsTemplate/editor")
    @ResponseBody
    public ResponseBase updateAssetsTemplate(HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            // 验证内容正确性
            Reply reply1 = mwAssetsSubTypeService.updateAssetsGroupId();
            reply = mwAssetsTemplateService.updateAssetsTemplate();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg() + ":" + reply1.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("updateAssetsTemplate", e);
            return setResultFail("MWAssetsTemplateController{} updateAssetsTemplate() error", "");
        }

        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assetsTemplate/fuzzSearchAllFiled/browse")
    @ResponseBody
    public ResponseBase fuzzSearchAllFiledData(@RequestBody AssetsSearchTermFuzzyParam param) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwAssetsTemplateService.fuzzSearchAllFiledData(param.getValue());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("MWAssetsTemplateController{} fuzzSearchAllFiledData() error", "");
        }

        return setResultSuccess(reply);
    }
}
