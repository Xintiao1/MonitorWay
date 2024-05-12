package cn.mw.monitor.assets.dao;

import cn.mw.monitor.assets.dto.MwAddAndUpdateTangibleAssetsTable;
import cn.mw.monitor.assets.dto.MwGetAssetsTemplateInfoDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface MwAssetsInfoSyncDao {
    List<Integer> getZabbixServerInfoIds();

    Integer countAssectByHostId(MwAddAndUpdateTangibleAssetsTable param);

    List<String> countAssectNotHostId(MwAddAndUpdateTangibleAssetsTable param);

    String getEnginemanageId(@Param("proxyId") String proxyId, @Param("serverId") Integer serverId);

    String getAssetsTemplateId(@Param("templateId") String templateId, @Param("serverId") Integer serverId);

    MwGetAssetsTemplateInfoDTO getAssetsTemplateInfoById(String assetsTemplateId);

    void InsertAssetsInfo(MwAddAndUpdateTangibleAssetsTable tangibleAssetsTable);

    void deleteAssetsInfoByRepeat(List<String> ids);

    /**
     * 查询资产主机ID与名称
     * @return
     */
    List<Map<String,Object>> selectAssetsIdAndName(@Param("tableName") String tableName);
}
