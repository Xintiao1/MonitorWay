package cn.mw.monitor.model.dao;

import cn.mw.monitor.model.dto.*;
import cn.mw.monitor.model.param.*;
import cn.mw.monitor.service.assets.model.AssetTypeIconDTO;
import cn.mw.monitor.service.assets.param.AddUpdateTangAssetsParam;
import cn.mw.monitor.service.model.dto.ModelInstanceBaseInfoDTO;
import cn.mw.monitor.service.model.param.AddModelInstancePropertiesParam;
import cn.mw.monitor.service.model.param.MwCustomColByModelDTO;
import cn.mw.monitor.service.model.param.MwModelInstanceParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author qzg
 * @date 2022/10/20
 */
public interface MwModelViewDao {
    MwModelScanResultSuccessParam getScanSuccessInfoById(Integer scanRuleId);

    String getModelIdGroups(Integer modelId);

    List<MwModelAssetsTreeDTO> getModelGroupByBase();

    List<AddAndUpdateModelParam> getModelIdByBase();

    List<Integer> getInstanceIdByBase(@Param("modelIndexList") List modelIndexList);

    List<ModelInstanceBaseInfoDTO> getModelIndexANDInstanceInfo(Boolean isBaseData);

    List<AddAndUpdateModelFirmParam> getVendorIcon();

    List<MwCustomColByModelDTO> getPropertiesIndexByCommon(@Param("userId") Integer userId, @Param("parentModelIds") String parentModelIds);

    List<AddModelInstancePropertiesParam> getPropertiesNameByMoreModel(@Param("parentModelIds") String parentModelIds);

    List<MwModelTemplateInfo> getAllTemplateName();

    List<MwModelTemplateInfo> getServerTemplateIdByName(MwModelTemplateInfo param);

    List<MwModelTemplateInfo> getTemplateInfoByMode(MwModelTemplateInfo param);

    String getServerGroupId(@Param("modelGroupId") Integer modelGroupId, @Param("monitorServerId") Integer monitorServerId);

    Integer getMonitorModelByModelGroup(AddUpdateTangAssetsParam param);

    List<MwModelTemplateInfo> getTemplateNameAndMonitorMode(MwModelTemplateInfo param);

    List<MwModelTemplateInfo> getTemplateNameByModeAndId();

    List<AddAndUpdateModelFirmParam> getBrandByTemplateName(String templateName);

    List<AddAndUpdateModelFirmParam> getSpecificationByTemplateNameAndBrand(MwModelTemplateInfo param);

    List<AddAndUpdateModelFirmParam> getDescriptionByTemplateSpecification(MwModelTemplateInfo param);

    Integer checkAuthenticationInfo(MwModelMacrosValInfoParam param);

    List<AddAndUpdateModelFirmParam> getAllVendorInfo();

    List<AddAndUpdateModelFirmParam> getAllSpecificationInfo();

    List<MwModelViewTreeDTO> getModelGroupTreeByView(@Param("instanceIdList") List<Integer> instanceIdList);

    List<AddModelInstancePropertiesParam> selectFiledProperticesByCommon();

    List<MwModelViewTreeDTO> getOrgInfoById(@Param("orgIds") List<Integer> orgIds);

    List<MwModelViewTreeDTO> getGroupInfoById(@Param("groupIds") List<Integer> groupIds);

    MwModelScanResultSuccessParam getScanTemplateInfoByICMP();

    List<MwModelTangibleAssetsDTO> getAllTangibleAssetsInfo();

    List<MwModelInfoDTO> getAllModelInfo();

    List<MwModelTangiblePermDTO> getAllUserPerInfoByAssets();

    List<MwModelTangiblePermDTO> getAllOrgPerInfoByAssets();

    List<MwModelTangiblePermDTO> getAllGroupPerInfoByAssets();

    Integer checkModelGroupExist(Integer groupId);

    List<MwModelSubTypeTable> selectAssetsTypeInfoById(@Param("ids") List<Integer> ids);

    void updateModelGroupId(@Param("modelGroupId") Integer modelGroupId, @Param("modelGroupName") String modelGroupName);

    void updateModelId(@Param("modelId") Integer modelId, @Param("finalModelId") Integer finalModelId);

    void updateModelProperties(@Param("modelId") Integer modelId, @Param("finalModelId") Integer finalModelId);

    void updateModelPagefield(@Param("modelId") Integer modelId, @Param("finalModelId") Integer finalModelId);

    List<AssetTypeIconDTO> selectAllAssetsTypeIcon();

    List<MwModelMacrosValInfoParam> getMacrosInfoByModel(List<Integer> list);

    List<MwModelMacrosValInfoParam> getMacrosInfoByName(MwModelMacrosValInfoParam param);

    void deleteMarcoInfoByModel(DeleteModelMacrosParam param);

    List<MwModelMacrosValInfoParam> getMacrosFieldByModel(Integer modelId);

    List<Integer> getModelIdByTemplateNameAndMode(MwModelTemplateInfo param);

    void saveMacroValAuthName(List<MwModelMacrosValInfoParam> list);

    List<MwModelInstanceParam> getGroupInstanceInfoByModelId(Integer modelId);

    List<MwModelInstanceParam> getSystemAndClassifyInstanceInfo(List<String> relationIndexs);

    List<MwModelUserDTO> getModelUserInfo(List<Integer> userIds);

    List<MwModelGroupDTO> getModelGroupInfo(@Param("loginName")String loginName);

    List<MwModelOrgDTO> getModelOrgInfo(List<Integer> orgIds);

}
