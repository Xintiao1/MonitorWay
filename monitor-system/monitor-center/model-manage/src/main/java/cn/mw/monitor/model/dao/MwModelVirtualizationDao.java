package cn.mw.monitor.model.dao;

import cn.mw.monitor.service.model.param.MwModelInstanceParam;
import cn.mw.monitor.service.model.param.AddModelInstancePropertiesParam;
import cn.mw.monitor.service.virtual.dto.VirtualizationDataInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author qzg
 * @date 2022/9/8
 */
public interface MwModelVirtualizationDao {

    List<AddModelInstancePropertiesParam> getModelInfoByModelIndex(String modelIndex);

    List<MwModelInstanceParam> queryVirualInstanceInfoByModelIndex(@Param("modelIndex") String modelIndex, @Param("relationInstanceId") Integer relationInstanceId);

    List<String> getAllVCenterInfo(Integer modelId);

    List<String> getPidsByModelIds(@Param("modelIdList") List<Integer> modelIdList);

    void saveVCenterInfo(List<VirtualizationDataInfo> list);

    List<Map<String, Object>> getAllVirtualDeviceInfo();

    void deleteVCenterInfo(List<VirtualizationDataInfo> list);

    String selectServerNameById(Integer monitorServerId);

    List<String> getAllModelIndexByBaseDevice();

    List<AddModelInstancePropertiesParam> getModelInfoByModelId(Integer modelId);

}
