package cn.mw.monitor.model.service;

import cn.mw.monitor.model.param.*;
import cn.mw.monitor.service.model.param.*;
import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.service.model.param.QueryEsParam;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author qzg
 * @date 2022/10/19
 */
public interface MwModelViewService {

    Reply getScanSuccessInfoById(QueryModelViewInstanceParam param);

    Reply getModelAssetsTreeInfo(QueryInstanceModelParam param);

    Reply getScanInfoByICMP(QueryModelViewInstanceParam param);

    Reply getSettingModuleInfo();

    Reply getMacInfoByTemplate(MwModelTemplateInfo param);

    Reply getTemplateInfoByMode(MwModelTemplateInfo param);

    boolean checkAuthenticationInfo(List<MwModelMacrosValInfoParam> params);

    Reply getAllTemplateName();

    Reply getTemplateNameAndMonitorMode(MwModelTemplateInfo param);

    Reply getMarcoInfoByTemplateNameAndMode(MwModelTemplateInfo param);

    Reply getMarcoInfoByModelId(MwModelTemplateInfo param);

    Reply deleteMarcoInfoByModel(DeleteModelMacrosParam param);

    Reply getBrandByTemplateName(MwModelTemplateInfo param);

    Reply getSpecificationByTemplateBrand(MwModelTemplateInfo param);

    Reply getAllVendorInfo();

    Reply getAllSpecificationInfo();

    Reply getDescriptionByTemplateSpecification(MwModelTemplateInfo param);

    Reply getModelCommonFields(String type);

    Reply getViewByFuzzyQuery();

    Reply getAuthInfoByModel(MwModelMacrosValInfoParam param);

    Reply saveAuthInfoByModel(List<MwModelMacrosValInfoParam> paramList);

    Reply addModelFieldGroupNodesByBase();

    Reply editorModelFieldToEs();

    List<Map<String, Object>> getWhetherExistsFieldModelInfo(QueryESWhetherExistField queryParam);

    Reply addModelFieldAssetsTypeIdByBase();

    Reply exportForExcel(QueryInstanceModelParam param, HttpServletRequest request, HttpServletResponse response);

    Reply syncAssetsInfoToES();

    Reply checkConnectAuto(List<MwModelMacrosValInfoParam> connectParam);

    Reply cancelManageAssetsToZabbix(List<CancelZabbixAssetsParam> params);

    List<Map<String, Object>> selectInstanceInfoByRelationInstanceId(QueryRelationInstanceModelParam param) throws Exception;

    List<Map<String, Object>> selectInstanceInfoByRelationInstanceIdList(QueryRelationInstanceModelParam param) throws Exception;

    List<Map<String, Object>> selectInstanceInfoByIdsAndModelIndexs(QueryRelationInstanceModelParam param) throws Exception;

    Reply getTriggerGetHostId(QueryModelAssetsTriggerParam param);

    void setModelInstanceValName();

    void setCabinetRelationId();

    List<Map<String, Object>> getAllInstanceInfoByModelIndexs(QueryEsParam param);
}
