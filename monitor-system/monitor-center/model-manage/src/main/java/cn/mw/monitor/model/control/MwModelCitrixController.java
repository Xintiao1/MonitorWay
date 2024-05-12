package cn.mw.monitor.model.control;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.model.param.citrix.MwAdvancedQueryCitrixParam;
import cn.mw.monitor.model.param.citrix.MwQueryCitrixParam;
import cn.mw.monitor.model.service.MwModelCitrixService;
import cn.mw.monitor.service.model.param.QueryModelInstanceParam;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author qzg
 * @date 2022/10/8
 */
@RequestMapping("/mwapi/modelCitrix")
@Controller
@Slf4j
@Api(value = "模型负载均衡接口", tags = "负载均衡接口")
public class MwModelCitrixController extends BaseApiService {

    @Autowired
    private MwModelCitrixService mwModelCitrixService;

    @Autowired
    private MwModelViewCommonService mwModelViewCommonService;

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/login/browse")
    @ResponseBody
    @ApiOperation(value = "Citrix登录，获取数据")
    public ResponseBase loginClientGetData(@RequestBody MwQueryCitrixParam param) {
        Reply reply;
        try {
            reply = mwModelCitrixService.loginClientGetData(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("loginClientGetData{}", e);
            return setResultFail("Citrix登录获取数据失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/citrixTree/browse")
    @ResponseBody
    @ApiOperation(value = "Citrix数据树结构")
    public ResponseBase getCitrixTreeInfo() {
        Reply reply;
        try {
            reply = mwModelCitrixService.getCitrixTreeInfo();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getCitrixTreeInfo{}", e);
            return setResultFail("Citrix数据树结构获取失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/citrixRelation/browse")
    @ResponseBody
    @ApiOperation(value = "获取Citrix数据关联信息")
    public ResponseBase getCitrixRelationList(@RequestBody MwQueryCitrixParam param) {
        Reply reply;
        try {
            //GSLB列表查询
            if (param.getIsGSLBQuery()!=null && param.getIsGSLBQuery()) {
                reply = mwModelCitrixService.getGSLBCitrixRelationList(param);
            } else {
                //LB列表查询
                reply = mwModelCitrixService.getLBCitrixRelationList(param);
            }
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getCitrixRelationList{}", e);
            return setResultFail("获取Citrix数据关联信息失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/export/template")
    @ResponseBody
    @ApiOperation("citrix关联数据导出")
    public ResponseBase exportCitrixRelationList(@RequestBody MwQueryCitrixParam param, HttpServletRequest request, HttpServletResponse response) {
        Reply reply;
        try {
            reply = mwModelCitrixService.exportCitrixRelationList(param, request, response);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("exportCitrixRelationList fail{}", e);
            return setResultFail("citrix关联数据导出失败", "");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getCitrixAssets/browse")
    @ResponseBody
    @ApiOperation("获取所有citrix数据")
    public ResponseBase getAllModelCitrixAssets() {
        Reply reply;
        try {
            reply = mwModelCitrixService.getAllModelCitrixAssets();
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("getAllModelCitrixAssets fail{}", e);
            return setResultFail("获取所有citrix数据失败", "");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/citrixAdvancedQuery/browse")
    @ResponseBody
    @ApiOperation("citrix数据高级设置查询")
    public ResponseBase advancedQueryCitrixInfo(@RequestBody MwAdvancedQueryCitrixParam param) {
        Reply reply;
        try {
            reply = mwModelCitrixService.advancedQueryCitrixInfo(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("getAllModelCitrixAssets fail{}", e);
            return setResultFail("citrix数据高级设置查询失败", "");
        }
    }


}
