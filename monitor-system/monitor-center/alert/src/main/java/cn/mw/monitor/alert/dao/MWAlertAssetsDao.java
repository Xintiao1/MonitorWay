package cn.mw.monitor.alert.dao;

import cn.mw.monitor.accountmanage.entity.AlertRecordTableDTO;
import cn.mw.monitor.service.assets.model.UserDTO;
import cn.mw.monitor.service.alert.dto.*;
import cn.mw.monitor.service.zbx.param.AlertParam;
import cn.mw.monitor.service.zbx.param.CloseDto;
import cn.mw.monitor.service.zbx.param.IgnoreAlertDto;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 *
 * @author xhy
 * @date 2020/4/22 17:10
 */
public interface MWAlertAssetsDao {
    List<String> getHostIdsByUserId(@Param("userId") Integer userId);

    List<Integer> getUserIds();

    AssetsDto getAssetsById(@Param("assetsId") String assetsId, @Param("monitorServerId") Integer monitorServerId);

    List<AssetsDto> getAssetsByIds(@Param("assetsIds") List<String> assetsIds);

    Integer getCountByUserIdAndTypeId(@Param("userId") Integer userId, @Param("assertTypeId") Integer assertTypeId);

    String getAssetsNameById(@Param("assetsId") String assetsId);

    Map<String, String> getAssetsNameAndIPById(@Param("assetsId") String assetsId, @Param("monitorServerId") Integer monitorServerId);

    List<String> getHostIds();

    List<Integer> getServerIds();

    List<Map> getWebMonitor(@Param("id") String id);

    List<Map> getLink(@Param("hostId") String hostId, @Param("monitorServerId") Integer monitorServerId,@Param("hostIp") String hostIp);

    List<AlertRecordTableDTO> getSendInfo(RecordParam param);

    int selectCountRecordTable(@Param("startTime") String startTime, @Param("endTime") String endTime);

    List<AlertRecordTableDTO> getSendInfoList(@Param("startTime") String startTime, @Param("endTime") String endTime,@Param("startNum") int startNum, @Param("endNum") int endNum);

    void deleteRecord(Date date);

    void deleteRecordInfo();

    List<Map> getMonitorServerName();

    void insertAlertSolutionTable(AlertReasonEditorParam param);

    void updateAlertSolutionTable(AlertReasonEditorParam param);

    Integer selectCountAlertSolutionTable(AlertReasonEditorParam param);

    AlertReasonEditorParam selectAlertSolutionTable(AlertReasonEditorParam param);

    void insertConfirmUserTable(AlertConfirmUserParam param);

    void insertConfirmUserTables(List<AlertConfirmUserParam> param);

    List<AlertReasonEditorParam> selectListAlertSolutionTable();

    List<AlertConfirmUserParam> selectConfirmByEventId(@Param("monitorServerId") Integer monitorServerId,@Param("eventid") String eventid);

    List<AlerUserOrgParam> selectOrgByUserIds(@Param("list") List<Integer> userIds);

    List<AlertConfirmUserParam> selectConfirmUserList();

    List<BussinessAlarmInfoParam> selectBussinessAlarmInfo();

    BussinessAlarmInfoParam selectBussinessAlarmInfoById(String dbid);

    void updateBussinessAlarmInfo(BussinessAlarmInfoParam param);

    List<ZbxAlertDto> getHuaXingAlert(@Param("startNum") int startNum, @Param("endNum") int endNum);

    int getHuaXingAlertCount();

    void deleteHuaXingAlert(@Param("date") String date);

    List<ZbxAlertDto> getHuaxingBuAlert(@Param("startNum") int startNum, @Param("endNum") int endNum);

    int getHuaxingBuAlertCount();

    void deleteHuaxingBuAlert(@Param("date") String date);

    List<String> getRecordHostIds(List<String> hostIds, Date startTime, Date endTime);

    List<AlertRecordTableDTO> getAlertRecordUserIds(@Param("ids") List<Integer> ids);

    List<AlertRecordTableDTO> getAlertRecordUserIdsList(@Param("ids") List<Integer> ids);

    int getAlertRecordUserIdsCount(@Param("startTime") String startTime, @Param("endTime") String endTime);

    void insertTriggercloseTable(@Param("list")List<CloseDto> params);

    void deleteTriggercloseTable(@Param("list")List<CloseDto> params);

    List<CloseDto> getTriggercloseTableTriggerIds();

    void insertIgnoreTable(@Param("list")List<IgnoreAlertDto> params);

    List<IgnoreAlertDto> selectIgnoreTable();

    void deleteIgnoreTable(@Param("list")List<IgnoreAlertDto> params);

    Integer selectCountRecordByDate(@Param("isSuccess")Integer isSuccess, @Param("startTime") String startTime, @Param("endTime") String endTime);

}
