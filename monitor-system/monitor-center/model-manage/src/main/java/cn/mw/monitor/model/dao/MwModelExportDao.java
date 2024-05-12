package cn.mw.monitor.model.dao;

import cn.mw.monitor.model.dto.MwModelPowerDTO;
import cn.mw.monitor.model.dto.MwModelViewTreeDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author qzg
 * @date 2021/2/6
 */
public interface MwModelExportDao {

    List<Integer> selectUserIdByName(String name);

    List<String> selectOrgIdByName(String orgName);

    List<Integer> selectGroupIdByName(String groupName);

    String selectMonitorServerId(String monitorServerName);

    String getOrgNameByExport(String orgIds);

    String getUserNameByExport(String userIds);

    String getGroupNameByExport(String groupIds);

    List<MwModelPowerDTO> selectUserIdInfo();

    List<MwModelPowerDTO> selectOrgIdInfo();

    List<MwModelPowerDTO> selectGroupIdInfo();

    List<MwModelViewTreeDTO> getOrgNameAllByExport();

    List<MwModelViewTreeDTO> getUserNameAllByExport();

    List<MwModelViewTreeDTO> getGroupNameAllByExport();

    List<MwModelViewTreeDTO> getAssetsTypeByExport();

    List<MwModelViewTreeDTO> getAssetsSubTypeByExport();

    List<MwModelViewTreeDTO> getServerNameByExport();

    List<MwModelViewTreeDTO> getMonitorModeByExport();

    List<MwModelViewTreeDTO> getAllProxyInfoByExport();

    Map getIndexInfoByGroup(@Param("groupName") String groupName, @Param("modelName") String modelName);

}
