package cn.mw.monitor.webMonitor.dao;

import cn.mw.monitor.service.user.dto.OrgDTO;
import cn.mw.monitor.service.webmonitor.model.HttpParam;
import cn.mw.monitor.webMonitor.api.param.webMonitor.AddUpdateWebMonitorParam;
import cn.mw.monitor.webMonitor.api.param.webMonitor.BatchUpdateParam;
import cn.mw.monitor.webMonitor.api.param.webMonitor.UpdateWebMonitorStateParam;
import cn.mw.monitor.webMonitor.dto.HostDto;
import cn.mw.monitor.webMonitor.dto.MwWebMonitorDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface MwWebmonitorTableDao {
    int delete(List<Integer> ids);

    int insert(AddUpdateWebMonitorParam record);

    MwWebMonitorDTO selectByPrimaryKey(Integer id);

    List<MwWebMonitorDTO> selectByPrimaryKeys(List<Integer> ids);

    int update(AddUpdateWebMonitorParam record);
    /**
     * 批量修改web信息
     *
     * @param record
     * @return
     */
    int updateBatch(BatchUpdateParam record);

    int updateUserState(UpdateWebMonitorStateParam updateWebMonitorStateParam);

    List<MwWebMonitorDTO> selectList(Map criteria);

    HostDto getHostIdAndServerId(String id);

    List<Integer> selectIds(List<String> ids);

    List<HttpParam> selectHttpIds(List<String> ids);

    int getMonitorFlagById(Integer id);

    HostDto getAssetsId(@Param("hostIp") String hostIp, @Param("monitorServerName")String monitorServerName);

    HostDto getAssetsIp(@Param("hostIp") String hostIp);

    List<Integer> selectUserIdsByUserNames(List<String> userNames);

    List<Integer> selectGroupIdsByGroupNames(List<String> groupNames);

    List<OrgDTO> selectOrgIdsByOrgNames(List<String> orgNames);

    List<Map<String,String>> fuzzSearchAllFiled(@Param("value") String value);
}
