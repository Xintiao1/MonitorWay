package cn.mw.monitor.model.control;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.model.param.*;
import cn.mw.monitor.model.service.MWModelZabbixMonitorService;
import cn.mw.monitor.model.service.MwModelAssestDiscoveryService;
import cn.mw.monitor.model.service.MwModelViewService;
import cn.mw.monitor.service.activitiAndMoudle.ModelServer;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.assets.param.AddUpdateTangAssetsParam;
import cn.mw.monitor.service.assets.param.QueryTangAssetsParam;
import cn.mw.monitor.service.model.param.*;
import cn.mw.monitor.service.model.service.MwModelCommonService;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.service.server.api.MwServerService;
import cn.mw.monitor.service.server.api.dto.NetListDto;
import cn.mw.monitor.service.server.param.AssetsIdsPageInfoParam;
import cn.mw.monitor.util.RSAUtils;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import com.google.common.base.Strings;
import com.google.gson.internal.$Gson$Preconditions;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author qzg
 * @date 2022/10/19
 */
@RequestMapping("/mwapi/modelView")
@Controller
@Slf4j
@Api(value = "资源中心", tags = "模型视图接口")
public class MwModelViewController extends BaseApiService {
    private final String DEPLOYMENTTYPES = "mw";
    @Autowired
    private MwModelViewService mwModelViewService;
    @Autowired
    private ModelServer modelSever;
    @Autowired
    private MwModelViewCommonService mwModelViewCommonService;
    @Autowired
    private  MwModelAssestDiscoveryService mwModelAssestDiscoveryService;
    @Autowired
    private MwModelCommonService mwModelCommonService;
    @Autowired
    private MWModelZabbixMonitorService mwModelZabbixMonitorService;

    //同步es字段groupNodes、assetsTypeId
    @Value("${SyncFieldByEs.isFlag}")
    private Boolean isSyncField;
    //同步原资产数据
    @Value("${SyncAssetsInfo.isFlag}")
    private Boolean isSyncAssets;
    //同步原资产数据
    @Value("${deployment.environment}")
    private String deploymentType;


    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getIsSyncField/browse")
    @ResponseBody
    @ApiOperation(value = "是否开启同步es字段接口")
    public ResponseBase getIsSyncField() {
        return setResultSuccess(isSyncField);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getDeploymentType/browse")
    @ResponseBody
    @ApiOperation(value = "是否显示实例拓扑tab页")
    public ResponseBase getDeploymentType() {
        boolean isShow = false;
        //猫维环境默认显示
        if (!Strings.isNullOrEmpty(deploymentType) && DEPLOYMENTTYPES.equals(deploymentType)) {
            isShow = true;
        }
        return setResultSuccess(isShow);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getIsSyncAssets/browse")
    @ResponseBody
    @ApiOperation(value = "是否开启同步原资产数据接口")
    public ResponseBase getIsSyncAssets() {
        return setResultSuccess(isSyncAssets);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/scanRuleId/browse")
    @ResponseBody
    @ApiOperation(value = "根据规则id获取扫描成功结果信息")
    public ResponseBase getScanSuccessInfoById(@RequestBody QueryModelViewInstanceParam param) {
        Reply reply = new Reply();
        try {
            reply = mwModelViewService.getScanSuccessInfoById(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getScanSuccessInfoById{}", e);
            return setResultFail("获取扫描成功结果信息失败","");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/scanICMPInfo/browse")
    @ResponseBody
    @ApiOperation(value = "ICMP扫描结果信息")
    public ResponseBase getScanInfoByICMP(@RequestBody QueryModelViewInstanceParam param) {
        Reply reply = new Reply();
        try {
            reply = mwModelViewService.getScanInfoByICMP(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getScanInfoByICMP{}", e);
            return setResultFail("ICMP扫描结果信息失败","");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/settingModule/browse")
    @ResponseBody
    @ApiOperation(value = "功能模块配置查询")
    public ResponseBase getSettingModuleInfo() {
        Reply reply = new Reply();
        try {
            reply = mwModelViewService.getSettingModuleInfo();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getScanInfoByICMP{}", e);
            return setResultFail("功能模块配置查询失败","");
        }
        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getAssetsTree/browse")
    @ResponseBody
    @ApiOperation(value = "获取资产实例树结构信息")
    public ResponseBase getModelAssetsTreeInfo(@RequestBody QueryInstanceModelParam param) {
        Reply reply = new Reply();
        try {
            reply = mwModelViewService.getModelAssetsTreeInfo(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getModelAssetsTreeInfo{}", e);
            return setResultFail("获取资产实例树结构信息失败","");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getMacInfo/browse")
    @ResponseBody
    @ApiOperation(value = "根据模板查询宏值")
    public ResponseBase getMacInfoByTemplate(@RequestBody MwModelTemplateInfo param) {
        Reply reply = new Reply();
        try {
            reply = mwModelViewService.getMacInfoByTemplate(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getMacInfoByTemplate{}", e);
            return setResultFail("根据模板查询宏值失败","");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getTemplateInfoByMode/browse")
    @ResponseBody
    @ApiOperation(value = "根据监控方式获取模板信息")
    public ResponseBase getTemplateInfoByMode(@RequestBody MwModelTemplateInfo param) {
        Reply reply = new Reply();
        try {
            reply = mwModelViewService.getTemplateInfoByMode(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getTemplateInfoByMode{}", e);
            return setResultFail("根据监控方式获取模板信息失败","");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getTemplateName/browse")
    @ResponseBody
    @ApiOperation(value = "获取所有模板名称")
    public ResponseBase getAllTemplateName() {
        Reply reply = new Reply();
        try {
            reply = mwModelViewService.getAllTemplateName();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getAllTemplateName{}", e);
            return setResultFail("获取所有模板名称失败","");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getTemplateNameByServerId/browse")
    @ResponseBody
    @ApiOperation(value = "获取所有模板名称")
    public ResponseBase getTemplateNameAndMonitorMode(@RequestBody MwModelTemplateInfo param) {
        Reply reply = new Reply();
        try {
            reply = mwModelViewService.getTemplateNameAndMonitorMode(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getTemplateNameAndMonitorMode{}", e);
            return setResultFail("获取所有模板名称失败","");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getMarcoInfoByModelId/browse")
    @ResponseBody
    @ApiOperation(value = "根据模型获取模型宏值数据")
    public ResponseBase getMarcoInfoByModelId(@RequestBody MwModelTemplateInfo param) {
        Reply reply = new Reply();
        try {
            reply = mwModelViewService.getMarcoInfoByModelId(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getMarcoInfoByModelId{}", e);
            return setResultFail("根据模型获取模型宏值数据失败","");
        }
        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "model_manage")
    @PostMapping("/marcoInfoByModel/delete")
    @ResponseBody
    @ApiOperation(value = "删除模型宏值数据")
    public ResponseBase deleteMarcoInfoByModel(@RequestBody DeleteModelMacrosParam param) {
        Reply reply = new Reply();
        try {
            reply = mwModelViewService.deleteMarcoInfoByModel(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getAssetsSubIdByTemplateName{}", e);
            return setResultFail("删除模型宏值数据失败","");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getBrandByTemplateName/browse")
    @ResponseBody
    @ApiOperation(value = "根据模板名称获取厂商信息")
    public ResponseBase getBrandByTemplateName(@RequestBody MwModelTemplateInfo param) {
        Reply reply = new Reply();
        try {
            reply = mwModelViewService.getBrandByTemplateName(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getBrandByTemplateName{}", e);
            return setResultFail("根据模板名称获取厂商信息失败","");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getSpecialByTemplateBrand/browse")
    @ResponseBody
    @ApiOperation(value = "根据模板名称规格信号获取规格型号信息")
    public ResponseBase getSpecificationByTemplateBrand(@RequestBody MwModelTemplateInfo param) {
        Reply reply = new Reply();
        try {
            reply = mwModelViewService.getSpecificationByTemplateBrand(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getSpecificationByTemplateBrand{}", e);
            return setResultFail("根据模板名称规格信号获取规格型号信息失败","");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getAllVendorInfo/browse")
    @ResponseBody
    @ApiOperation(value = "获取所有的厂商信息")
    public ResponseBase getAllVendorInfo() {
        Reply reply = new Reply();
        try {
            reply = mwModelViewService.getAllVendorInfo();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getAllVendorInfo{}", e);
            return setResultFail("获取所有的厂商信息失败","");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getAllSpecificationInfo/browse")
    @ResponseBody
    @ApiOperation(value = "获取所有的规格型号信息")
    public ResponseBase getAllSpecificationInfo() {
        Reply reply = new Reply();
        try {
            reply = mwModelViewService.getAllSpecificationInfo();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getAllSpecificationInfo{}", e);
            return setResultFail("获取所有的规格型号信息失败","");
        }
        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getDescByTemplateSpecial/browse")
    @ResponseBody
    @ApiOperation(value = "根据模板名称规格信号获取描述信息")
    public ResponseBase getDescByTemplateSpecification(@RequestBody MwModelTemplateInfo param) {
        Reply reply = new Reply();
        try {
            reply = mwModelViewService.getDescriptionByTemplateSpecification(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getDescByTemplateSpecification{}", e);
            return setResultFail("根据模板名称规格信号获取描述信息失败","");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getModelCommonFields/browse")
    @ResponseBody
    @ApiOperation(value = "获取资产视图列表字段")
    public ResponseBase getModelCommonFieldsByView(@RequestBody MwInstanceFieldShowType param) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwModelViewService.getModelCommonFields(param.getType());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getModelCommonFields{}", e);
            return setResultFail("获取资产视图列表字段失败", "获取资产视图列表字段失败");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getModelListInfo/browse")
    @ResponseBody
    @ApiOperation(value = "获取资产视图列表数据")
    public ResponseBase getModelListInfoByView(@RequestBody QueryInstanceModelParam param) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwModelViewCommonService.getModelListInfoByView(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getModelListInfoByView{}", e);
            return setResultFail("获取资产视图列表数据失败", "获取资产视图列表数据失败");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/viewByFuzzyQuery/browse")
    @ResponseBody
    @ApiOperation(value = "模型实例模糊查询下拉信息提示")
    public ResponseBase getViewByFuzzyQuery() {
        Reply reply;
        try {
            reply = mwModelViewService.getViewByFuzzyQuery();
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("getViewByFuzzyQuery{}", e);
            return setResultFail("模型实例模糊查询下拉信息提示失败", "模型实例模糊查询下拉信息提示失败");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getAuthInfo/browse")
    @ResponseBody
    @ApiOperation(value = "获取模型对应的凭证信息")
    public ResponseBase getAuthInfoByModel(@RequestBody MwModelMacrosValInfoParam param) {
        Reply reply;
        try {
            reply = mwModelViewService.getAuthInfoByModel(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("getAuthInfoByModel{}", e);
            return setResultFail("获取模型对应的凭证信息失败", "获取模型对应的凭证信息失败");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/saveAuthInfo/insert")
    @ResponseBody
    @ApiOperation(value = "保存模型对应的凭证信息")
    public ResponseBase saveAuthInfoByModel(@RequestBody List<MwModelMacrosValInfoParam> paramList) {
        Reply reply;
        try {
            reply = mwModelViewService.saveAuthInfoByModel(paramList);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("saveAuthInfoByModel{}", e);
            return setResultFail("保存模型对应的凭证信息失败", "保存模型对应的凭证信息失败");
        }
    }


    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getExistsFieldModelInfo/editor")
    @ResponseBody
    @ApiOperation(value = "获取资产实例指定的字段")
    public ResponseBase getExistsFieldModelInfo(@RequestBody QueryESWhetherExistField queryParam) {
        Reply reply;
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            // 验证内容正确性
            list = mwModelViewService.getWhetherExistsFieldModelInfo(queryParam);
        } catch (Throwable e) {
            log.error("addModelFieldGroupNodesByBase{}", e);
            return setResultFail("资产设施模型新增GroupNodes字段失败", "资产设施模型新增GroupNodes字段失败");
        }
        return setResultSuccess(list);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/addModelGroupNodesField/editor")
    @ResponseBody
    @ApiOperation(value = "资产设施模型新增GroupNodes字段")
    public ResponseBase addModelFieldGroupNodesByBase() {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwModelViewService.addModelFieldGroupNodesByBase();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("addModelFieldGroupNodesByBase{}", e);
            return setResultFail("资产设施模型新增GroupNodes字段失败", "资产设施模型新增GroupNodes字段失败");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/editorModelFieldToEs/editor")
    @ResponseBody
    @ApiOperation(value = "更新模型数据到es数据库中")
    public ResponseBase editorModelFieldToEs() {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwModelViewService.editorModelFieldToEs();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("editorModelFieldToEs{}", e);
            return setResultFail("更新模型数据到es数据库失败", "更新模型数据到es数据库失败");
        }
        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "model_manage")
    @PostMapping("/addModelAssetsTypeField/editor")
    @ResponseBody
    @ApiOperation(value = "资产设施模型新增资产类型字段")
    public ResponseBase addModelFieldAssetsTypeIdByBase() {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwModelViewService.addModelFieldAssetsTypeIdByBase();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("addModelFieldAssetsTypeIdByBase{}", e);
            return setResultFail("资产设施模型新增资产类型字段失败", "资产设施模型新增资产类型字段失败");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/export/InstanceInfoByMoreModel")
    @ResponseBody
    @ApiOperation("资产视图数据导出Excel接口")
    public ResponseBase exportForExcel(@RequestBody QueryInstanceModelParam param, HttpServletRequest request, HttpServletResponse response) {
        Reply reply;
        try {
            param.setIsAssetsView(true);
            reply = mwModelViewService.exportForExcel(param, request, response);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("exportForExcel fail{}", e);
            return setResultFail("资产视图数据导出Excel失败失败","");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/syncAssetsInfo/create")
    @ResponseBody
    @ApiOperation("原资产数据更新同步")
    public ResponseBase syncAssetsInfoToES() {
        Reply reply;
        try {
            reply = mwModelViewService.syncAssetsInfoToES();
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("syncAssetsInfoToES fail{}", e);
            return setResultFail("原资产数据更新同步失败失败","");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getCommonQueryInfo/browse")
    @ResponseBody
    @ApiOperation(value = "es资产公共查询接口")
    public ResponseBase getCommonQueryInfo(@RequestBody QueryModelAssetsParam param) {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            list = mwModelViewCommonService.getModelListInfoByPerm(param);
        } catch (Throwable e) {
            log.error("getModelAssetsTreeInfo{}", e);
            return setResultFail("资产公共查询接口失败","");
        }
        return setResultSuccess(list);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/connectCheck/browse")
    @ResponseBody
    @ApiOperation(value = "同步凭证校验")
    public ResponseBase checkConnectAuto(@RequestBody List<MwModelMacrosValInfoParam> connectParam) {
        Reply reply;
        try {
            reply = mwModelViewService.checkConnectAuto(connectParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("CheckConnectAuto{}", e);
            return setResultFail("同步凭证校验失败","");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/modelAssets/cancelManage")
    @ResponseBody
    @ApiOperation(value = "取消资产纳管")
    public ResponseBase cancelManageAssetsToZabbix(@RequestBody List<CancelZabbixAssetsParam> params) {
        Reply reply;
        try {
            reply = mwModelViewService.cancelManageAssetsToZabbix(params);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("cancelManageAssetsToZabbix{}", e);
            return setResultFail("取消资产纳管失败","");
        }
        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getModelSystemAndClassify/browse")
    @ResponseBody
    @ApiOperation(value = "查询业务系统业务分类模型信息")
    public ResponseBase getModelSystemAndClassify() {
        Reply reply = null;
        try {
            reply = mwModelViewCommonService.getModelSystemAndClassify();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getModelSystemAndClassify{}", e);
            return setResultFail("查询业务系统业务分类模型信息失败","");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getSystemClassifyInstance/browse")
    @ResponseBody
    @ApiOperation(value = "查询业务系统分组下的所有实例信息")
    public ResponseBase getSystemAndClassifyInstanceInfo() {
        Reply reply = null;
        try {
            reply = mwModelViewCommonService.getSystemAndClassifyInstanceInfo();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getSystemAndClassifyInstanceInfo{}", e);
            return setResultFail("查询业务系统业务分类模型信息失败","");
        }
        return setResultSuccess(reply);
    }

    @Autowired
    private MwServerService service;
    @MwPermit(moduleName = "model_manage")
    @PostMapping("/modelAssets/test")
    @ResponseBody
    @ApiOperation(value = "测试接口")
    public ResponseBase testController() {
        Reply reply = null;
        try {
            mwModelViewCommonService.getAllRancherProjectUserInfo();
//           mwModelCommonService.getAllInstanceInfoByCabinet();
        } catch (Throwable e) {
            log.error("CheckConnectAuto{}", e);
            return setResultFail("", "");
        }
        return setResultSuccess(reply);
    }

    /**
     * 开放接口
     *
     * @param param
     * @return
     */
    @PostMapping("/open/getModel/browse")
    @ResponseBody
    @ApiOperation(value = "获取主机ID与服务器ID")
    public ResponseBase getAssetsIdAndServerId(@RequestBody QueryInstanceModelOpenParam param) {
        Reply reply;
        try {
            String privateKey = RSAUtils.RSA_PRIVATE_KEY;
            String pageNumber = RSAUtils.decryptData(param.getPageNumber(), privateKey);
            String pageSize = RSAUtils.decryptData(param.getPageSize(), privateKey);
            if (!NumberUtils.isNumber(pageNumber) || !NumberUtils.isNumber(pageSize)) {
                return setResultFail("数据不符合规则,解密数据失败", "");
            }
            QueryInstanceModelParam qParam = new QueryInstanceModelParam();
            qParam.setPageNumber(Integer.parseInt(pageNumber));
            qParam.setPageSize(Integer.parseInt(pageSize));
            // 验证内容正确性
            reply = mwModelViewCommonService.getModelAssetsHostData(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getAssetsIdAndServerId", e);
            return setResultFail("获取数据失败", "获取数据失败");
        }
        return setResultSuccess(reply);
    }

    /**
     * 开放接口
     *
     * @return
     */
    @GetMapping("/open/citrixList/browse")
    @ResponseBody
    @ApiOperation(value = "获取所有citrix资产的关联数据")
    public ResponseBase getAllCitrixListRelationInfo() {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwModelViewCommonService.getAllCitrixListRelationInfo();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getAllCitrixListRelationInfo", e);
            return setResultFail("获取所有citrix资产的关联数据失败", "获取所有citrix资产的关联数据失败");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getTriggerByAssets/browse")
    @ResponseBody
    @ApiOperation(value = "根据资产查询触发器阈值信息")
    public ResponseBase getTriggerGetHostId(@RequestBody QueryModelAssetsTriggerParam param) {
        Reply reply = null;
        try {
            reply = mwModelViewService.getTriggerGetHostId(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("CheckConnectAuto{}", e);
            return setResultFail("根据资产查询触发器阈值信息失败","");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/setModelInstanceValName/editor")
    @ResponseBody
    @ApiOperation(value = "设置ES数据name字段")
    public ResponseBase setModelInstanceValName() {
        try {
            mwModelViewService.setModelInstanceValName();
        } catch (Throwable e) {
            log.error("setModelInstanceValName\n{}", e);
            return setResultFail("设置ES数据失败","");
        }
        return setResultSuccess();
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/setCabinetRelationId/editor")
    @ResponseBody
    @ApiOperation(value = "设置机房机柜关联Id")
    public ResponseBase setRoomAndCabinetRelationId() {
        try {
            mwModelViewService.setCabinetRelationId();
        } catch (Throwable e) {
            log.error("setRoomAndCabinetRelationId{}", e);
            return setResultFail("设置机房机柜关联数据失败","");
        }
        return setResultSuccess();
    }

}
