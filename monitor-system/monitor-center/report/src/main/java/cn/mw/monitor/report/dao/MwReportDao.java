package cn.mw.monitor.report.dao;

import cn.mw.monitor.report.dto.*;
import cn.mw.monitor.report.dto.assetsdto.AssetByTypeDto;
import cn.mw.monitor.report.dto.assetsdto.RunTimeItemValue;
import cn.mw.monitor.report.dto.linkdto.*;
import cn.mw.monitor.report.param.ReportCountParam;
import cn.mw.monitor.report.param.ReportMessageMapperParam;
import cn.mw.monitor.report.param.ReportUserParam;
import cn.mw.monitor.util.entity.EmailFrom;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author xhy
 * @date 2020/5/9 16:58
 */
public interface MwReportDao {
    int insertTime(SolarTimeDto solarTimeDto);

    int deleteTime(Integer id, Integer type);

    SolarTimeDto selectTime(Integer type);

    String insertReportTable(CreatAndUpdateReportParam creatAndUpdateReportParam);

    Integer updateReportTable(CreatAndUpdateReportParam creatAndUpdateReportParam);

    List<ReportTypeDto> getReportType();

    List<ReportTimeTaskDto> getReportTimeTask();

    List<ReportActionDto> getReportAction();

    Integer insertReportTimeActionMapper(List<MwTimeActionMapper> timeActionMappers);

    Integer insertReportRuleMapper(List<MwReportRuleMapper> reportRuleMappers);


    Integer deleteReportTimeActionMapper(String reportId);

    Integer deleteReportTimeRuleMapper(String reportId);

    Integer deleteReportTable(@Param("reportIds") List<String> reportIds);

//    Integer deleteReportUserListMapper(List<Integer> reportIds);

//    Integer deleteReportGroupListMapper(List<Integer> reportIds);

    Integer deleteReportTimeActionListMapper(List<Integer> timeIds);

    MwReportTable selectById(String assetsId);

    List<MwReportTable> selectPubReport(Map pubCriteria);

    List<MwReportTable> selectPriReport(Map priCriteria);

    List<Integer> selectTimeIds(List<Integer> reportIdList);


    Integer insertReportNetWork(@Param("tableName") String tableName, @Param("dateTime") Date dateTime, @Param("trendNetDtos") List<NetWorkDto> trendNetDtos);


    Integer insertReportDisk(@Param("tableName") String tableName, @Param("dateTime") Date dateTime, @Param("diskTrends") List<DiskDto> diskTrends);

    Integer insertReportCpuAndMemory(@Param("tableName") String tableName, @Param("dateTime") Date dateTime, @Param("cpuAndMemoryTrends") List<CpuAndMemoryDtos> cpuAndMemoryTrends);

    Integer insertReportLink(@Param("tableName") String tableName, @Param("dateTime") Date dateTime, @Param("links") List<InterfaceReportDtos> workLinks);


    List<DiskDto> selectDiskList(QueryDto queryDto);

    List<CpuAndMemoryDtos> selectCpuAndMemoryList(QueryDto queryDto);

    List<NetWorkDto> selectNetWorkList(QueryDto queryDto);

    List<InterfaceReportDtos> selectLinkList(QueryDto queryDto);

    int getTimeDateCount(@Param("allDayStartTime") String allDayStartTime, @Param("allDayEndTime") String allDayEndTime);

    int getReportTimeDateCount(@Param("tableName") String tableName, @Param("allDayStartTime") String allDayStartTime, @Param("allDayEndTime") String allDayEndTime);

    int selectSolarDayCount(String date);

    //查询每日一次的定时报表
    List<MwReportTable> selectOneDayReport();

    List<MwReportTable> selectTimeData(@Param("reportId") int reportId);

    //查询信息发送方映射表
    List<ReportMessageMapperParam> selectMessageMapperReport(@Param("id") String id);

    //查询报表负责人
    List<ReportUserParam> selectPriLists(@Param("lists") List<Integer> lists);

    EmailFrom selectEmailFromReport(@Param("id") String id);

    int insertRecord(ReportRecordTable alertRecordTable);


    List<ReportCountDto> selectPubReportCount(ReportCountParam reportCountParam);

    List<ReportCountDto> selectPriReportCount(ReportCountParam reportCountParam);


    List<MwHistoryDto> selectInHistoryList(LinkHistoryParam linkHistoryParam);

    List<MwHistoryDto> selectOutHistoryList(LinkHistoryParam linkHistoryParam);

    List<LinkDetailDto> groupSelectList(MwGroupDto groupDto);

    List<AssetByTypeDto> selectAeestInfo(Date dateStart,Date dateEnd);

    void deleteRunTimeStatus(@Param("param")Integer param);

    void insertRunTimeStatus(@Param("list") List<RunTimeItemValue> list, @Param("param") Integer param, @Param("date") Date date);

    List<RunTimeItemValue> selectRuntimeByTypeAndName(@Param("name")String name, @Param("type")Integer type);



    List<RunTimeItemValue> getAllRunTimeItemValue(@Param("name")String s, @Param("startTime")Date startTime, @Param("endTime")Date endTime);

    void deleteReportSendUser(@Param("reportId") String reportId);

    int insertReportSendUser( @Param("dto") List<MWReportSendUserDto> sendUserDtos);

    List<MWReportSendUserDto> selectReportSendUser(@Param("reportId") String reportId);

    List<UserDTO> selectUserNews(@Param("ids") List<Integer> ids);

    List<GroupDTO> selectUserGroupNews(@Param("ids") List<Integer> ids);

    /**
     * 查询标签“月度报表”所对应的资产
     * @return
     */
    List<Map<String,String>> selectLabelAssets();

    /**
     * 查询报表通知用户组
     * @param reportId
     * @return
     */
    List<Integer> selectSendEmailUserGroup(String reportId);

    /**
     * 获取资产标签为区域的资产
     * @return
     */
    List<PatrolInspectionDto> selectAssetsByLabel(String labelName);

    /**
     * 根据类型获取各种数据安全门限
     */
    List<MwReportSafeValueDto> selectSafeValueByType(@Param("typeId") Integer typeId);

    /**
     * 获取线路流量历史
     * @return
     */
    List<InterfaceReportDtos> getLinkReportHistory();

    /**
     * 删除线路流量历史
     * @return
     */
    void deleteLinkReportHistory();

    /**
     * 添加线路流量历史
     * @return
     */
    void insertLinkReportHistory(@Param("links") List<InterfaceReportDtos> dtos);

    /**
     * 查询报表的指标信息
     * @return
     */
    List<MwReportIndexDto> selectReportIndex();

    /**
     * 新增报表历史数据缓存
     * @param trendCacheDtos
     * @return
     */
    int insertReportHistoryData(@Param("dtos") List<MwReportTrendCacheDto> trendCacheDtos);

    /**
     * 缓存最新数据
     * @param trendCacheDtos
     * @return
     */
    int insertReportLatestData(@Param("dtos") List<MwReportTrendCacheDto> trendCacheDtos);

    /**
     * 删除存储的最新数据
     */
    void deleteReportLatestData();


    /**
     * 获取报表指标的最新数据
     * @param assetsIds
     * @param itemNames
     * @return
     */
    List<MwReportTrendCacheDto> selectReportLatestData(@Param("assetsIds") List<String> assetsIds,@Param("itemNames") List<String> itemNames);

    /**
     * 获取报表指标的历史数据
     * @param assetsIds
     * @param itemNames
     * @return
     */
    List<MwReportTrendCacheDto> selectReporHistoryData(@Param("assetsIds") List<String> assetsIds,@Param("itemNames") List<String> itemNames,@Param("startTime") Long startTime,@Param("endTime") Long endTime);
}
