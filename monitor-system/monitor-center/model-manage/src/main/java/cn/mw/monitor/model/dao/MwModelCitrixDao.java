package cn.mw.monitor.model.dao;

import cn.mw.monitor.model.dto.ModelInstanceDto;
import cn.mw.monitor.model.dto.MwModelInfoDTO;
import cn.mw.monitor.service.model.param.MwModelInstanceParam;
import cn.mw.monitor.service.model.param.AddModelInstancePropertiesParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author qzg
 * @date 2022/10/9
 */
public interface MwModelCitrixDao {
    List<AddModelInstancePropertiesParam> getModelProperticewInfoByName(String modelName);

    List<MwModelInfoDTO> getModelIndexByName();

    MwModelInfoDTO getModelIndexByModelName(String modelName);

    List<MwModelInstanceParam> getModelInstanceInfoByName(@Param("citrixModelId")String citrixModelId, @Param("instanceId") Integer instanceId);

    List<MwModelInfoDTO> getAllCitrixModelInfo();

    List<ModelInstanceDto> getAllCitrixInstanceInfo();

    MwModelInfoDTO getModelIndexInfo(String modelName);

}
