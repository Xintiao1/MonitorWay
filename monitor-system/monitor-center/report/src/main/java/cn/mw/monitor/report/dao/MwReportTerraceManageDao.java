package cn.mw.monitor.report.dao;

import cn.mw.monitor.report.dto.DiskDto;
import cn.mw.monitor.report.dto.MWMplsCacheDataDto;
import cn.mw.monitor.report.param.IpAddressReport;
import cn.mw.monitor.report.dto.TrendDiskDto;
import cn.mw.monitor.report.dto.assetsdto.RunTimeItemValue;
import cn.mw.monitor.report.param.LineFlowReportParam;
import cn.mw.monitor.report.param.MwAssetsUsabilityParam;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @ClassName MwReportTerraceManageDao
 * @Description ToDo
 * @Author gengjb
 * @Date 2021/10/13 14:15
 * @Version 1.0
 **/
public interface MwReportTerraceManageDao {

    List<Map<String,String>> getAssetsOrgData(@Param("type") String type, @Param("assetsIds") List<String> assetsIds);

    List<DiskDto> getDiskNews(@Param("assetsIds") List<String> assetsIds, @Param("startTime") String startTime, @Param("endTime") String endTime);

    List<Integer> getLabelData(@Param("id") String id);

    List<IpAddressReport> selectIPaddresshis(@Param("label") String label, @Param("i") int i, @Param("strings") List<String> strings, @Param("sd") Date sd, @Param("ed") Date ed,  @Param("order")String order,@Param("property") String property);

    List<Map<String,Object>> selectAssetsNanoTube(@Param("ips") List<String> ips);

    List<Map<String,Object>> getIpAddress(@Param("ids") List<String> ids, @Param("status") Integer status);

    List<IpAddressReport> getIpAddressNew(@Param("ids") List<String> ids, @Param("status") Integer status);

    List<IpAddressReport> selectEatAssets(@Param("label") String label, @Param("i") int i, @Param("strings") List<String> strings, @Param("sd") Date sd, @Param("ed") Date ed, @Param("order")String order,@Param("property") String property);

    Map<String, Object> getlinkid(@Param("linkid") Integer linkid);

    /**
     * 流量统计报表缓存数据
     * @param params
     * @return
     */
    int saveLinkFlowCacheData(@Param("params") List<LineFlowReportParam> params);

    /**
     * 删除流量统计报表缓存数据
     */
    void deleteLinkFlowCacheData(Integer dateType);

    /**
     * 磁盘使用情况报表数据缓存
     * @param params
     * @return
     */
    int saveDiskUseCacheData(@Param("params") List<TrendDiskDto> params);

    /**
     * 删除磁盘使用情况报表数据缓存
     */
    void deleteDiskUseCacheData(Map map);


    /**
     * Cpu与内存报表
     * @param params
     * @return
     */
    int saveCpuAndMemoryData(@Param("params") List<RunTimeItemValue> params);

    void deleteCpuAndMemoryData();

    int saveAssetsUsabilityData(@Param("params") List<MwAssetsUsabilityParam> params, @Param("date")  Date date);

    void deleteAssetsUsabilityData();

    List<RunTimeItemValue> selectCpuAndMemoryData(@Param("dateType") Integer dateType,@Param("ids") List<String> ids);

    List<LineFlowReportParam> selectLinkFlowData(@Param("dateType") Integer dateType,@Param("ids") List<String> ids);

    List<TrendDiskDto> selectDiskUseData(@Param("dateType") Integer dateType,@Param("ids") List<String> ids);

    List<MwAssetsUsabilityParam> selectAssetsUsabilityData(@Param("start") Date aLong, @Param("end") Date aLong1, @Param("ids") List<String> ids, @Param("i")int i);

    List<Date> selectAssetsUsabilityByDate(@Param("startTime")String startTime,@Param("date") Date date);


    List<Date> selectRunTimeByBelongTime(@Param("startTime")String startTime,@Param("date") Date date);
    List<MwAssetsUsabilityParam> selectAssetsUsabilityData(@Param("dateType") Integer dateType,@Param("ids") List<String> ids);

    List<Date> selectLineFlowCount();

    List<LineFlowReportParam> selectDateSectionData(@Param("startTime") String startTime,@Param("endTime") String endTime,@Param("ids") List<String> ids);

    List<Date> selectCpuAndMemoryCount();

    List<RunTimeItemValue> selectCpuAndMemoryDateSectionData(@Param("startTime") String startTime,@Param("endTime") String endTime,@Param("ids") List<String> ids);

    int saveMplsHistoryData(@Param("data") String data,@Param("saveTime") Date saveTime,@Param("name") String name);

    int saveMplsHistoryDataCache(@Param("sendData") String sendData,@Param("acceptData") String acceptData,@Param("saveTime") Date saveTime,@Param("name") String name,@Param("sortSendData") String sortSendData,@Param("sortAcceptData") String sortAcceptData);


    List<Date> selectMplsHistoryTime();

    List<Map<String,String>> selectMplsLineHistoryData(@Param("startTime") String startTime,@Param("endTime") String endTime,@Param("lineName") String lineName,@Param("type") int type);

    //添加线路流量统计报表天级数据
    int saveLinkReportDaily(@Param("params") List<LineFlowReportParam> params);
    //添加线路流量统计报表周级数据
    int saveLinkReportWeekly(@Param("params") List<LineFlowReportParam> params);
    //添加线路流量统计报表月级数据
    int saveLinkReportMonthly(@Param("params") List<LineFlowReportParam> params);

    //查询线路流量统计报表天级数据
    List<LineFlowReportParam> selectLinkReportDailyData(@Param("startTime") Date startTime,@Param("endTime") Date endTime,@Param("ids") List<String> ids);
    //查询线路流量统计报表周级数据
    List<LineFlowReportParam> selectLinkReportWeeklyData(@Param("weekDate") String weekDate,@Param("ids") List<String> ids);
    //查询线路流量统计报表月级数据
    List<LineFlowReportParam> selectLinkReportMonthlyData(@Param("monthDate") String monthDate,@Param("ids") List<String> ids);

    //添加cpu天级数据
    int saveCpuAndMemoryReportDaily(@Param("params") List<RunTimeItemValue> params);
    //添加cpu周级数据
    int saveCpuAndMemoryReportWeekly(@Param("params") List<RunTimeItemValue> params,@Param("weekDate") String weekDate);
    //添加cpu月级数据
    int saveCpuAndMemoryReportMonthly(@Param("params") List<RunTimeItemValue> params,@Param("monthDate") String monthDate);

    //查询CPU与内存报表天级数据
    List<RunTimeItemValue> selectCpuAndMemoryReportDailyData(@Param("startTime") Date startTime,@Param("endTime") Date endTime,@Param("ids") List<String> ids);
    //查询CPU与内存报表周级数据
    List<RunTimeItemValue> selectCpuAndMemoryReportWeellyData(@Param("weekDate") String weekDate,@Param("ids") List<String> ids);
    //查询CPU与内存报表月级数据
    List<RunTimeItemValue> selectCpuAndMemoryReportMonthlyData(@Param("monthDate") String monthDate,@Param("ids") List<String> ids);

    //添加磁盘使用情况天级数据
    int saveDiskUseReportDaily(@Param("params") List<TrendDiskDto> params);
    //添加磁盘使用情况周级数据
    int saveDiskUseReportWeekly(@Param("params") List<TrendDiskDto> params,@Param("weekDate") String weekDate);
    //添加磁盘使用情况月级数据
    int saveDiskUseReportMonthly(@Param("params") List<TrendDiskDto> params,@Param("monthDate") String monthDate);

    //查询磁盘使用情况数据
    List<TrendDiskDto> selectDiskUseReportDailyData(@Param("startTime") Date startTime,@Param("endTime") Date endTime,@Param("ids") List<String> ids);
    //查询磁盘使用情况周数据
    List<TrendDiskDto> selectDiskUseReportWeeklyData(@Param("weekDate") String weekDate,@Param("ids") List<String> ids);
    //查询磁盘使用情况月数据
    List<TrendDiskDto> selectDiskUseReportMonthlyData(@Param("monthDate") String monthDate,@Param("ids") List<String> ids);

    //添加资产可用性天级数据
    int saveAssetUsabilityReportDaily(@Param("params") List<MwAssetsUsabilityParam> params);
    //添加资产可用性周级数据
    int saveAssetUsabilityReportWeekly(@Param("params") List<MwAssetsUsabilityParam> params,@Param("weekDate") String weekDate);
    //添加资产可用性月级数据
    int saveAssetUsabilityReportMonthly(@Param("params") List<MwAssetsUsabilityParam> params,@Param("monthDate") String monthDate);

    //查询资产可用性天数据
    List<MwAssetsUsabilityParam> selectAssetsUsabilityDailyData(@Param("startTime") Date startTime,@Param("endTime") Date endTime,@Param("ids") List<String> ids);
    //查询资产可用性周数据
    List<MwAssetsUsabilityParam> selectAssetsUsabilityWeeklyData(@Param("weekDate") String weekDate,@Param("ids") List<String> ids);
    //查询资产可用性月数据
    List<MwAssetsUsabilityParam> selectAssetsUsabilityMonthlyData(@Param("monthDate") String monthDate,@Param("ids") List<String> ids);

    //添加运行状态报表天数据
    int saveRunStateReportDaily(@Param("params") List<RunTimeItemValue> runTimeItemValues);
    //添加运行状态报表周数据
    int saveRunStateReportWeekly(@Param("params") List<RunTimeItemValue> runTimeItemValues,@Param("weekDate") String weekDate);
    //添加运行状态报表月数据
    int saveRunStateReportMonthly(@Param("params") List<RunTimeItemValue> runTimeItemValues,@Param("monthDate") String monthDate);

    //查询运行状态报表天数据
    List<RunTimeItemValue> selectRunStateDailyData(@Param("startTime") Date startTime,@Param("endTime") Date endTime);
    //查询运行状态报表天数据
    List<RunTimeItemValue> selectRunStateNameDailyData(@Param("name") String name,@Param("startTime") Date startTime,@Param("endTime") Date endTime);
    //查询运行状态报表周数据
    List<RunTimeItemValue> selectRunStateWeeklyData(@Param("weekDate") String weekDate);

    //添加MPLS历史数据天数据
    int saveMplsHistoryReportDaily(@Param("sendData") String sendData,@Param("acceptData") String acceptData,@Param("saveTime") Date saveTime,@Param("name") String name,@Param("sortSendData") String sortSendData,@Param("sortAcceptData") String sortAcceptData,@Param("updateSuccess") boolean updateSuccess);
    //添加MPLS历史数据周数据
    int saveMplsHistoryReportWeekly(@Param("params") List<MWMplsCacheDataDto> mwMplsCacheDataDtos);
    //添加MPLS历史数据月数据
    int saveMplsHistoryReportMonthly(@Param("params") List<MWMplsCacheDataDto> mwMplsCacheDataDtos);
    //查询MPLS报表天数据
    List<Map<String,String>> selectMplsLineHistoryDailyData(@Param("startTime") Date startTime,@Param("endTime") Date endTime,@Param("linkName") String linkName);
    //查询MPLS报表天数据
    List<Map<String,String>> selectMplsLineHistoryDailyDataToo(@Param("startTime") Date startTime,@Param("endTime") Date endTime,@Param("linkName") String linkName);
    //查询MPLS报表周数据
    List<MWMplsCacheDataDto> selectMplsLineHistoryWeeklyData(@Param("weekDate") String weekDate,@Param("linkName") String linkName);
    //查询MPLS报表月数据
    List<MWMplsCacheDataDto> selectMplsLineHistoryMonthlyData(@Param("monthDate") String monthDate,@Param("linkName") String linkName);
    //根据表名查询不同报表的日期集合
    List<Date> selectReportDate(@Param("tableName") String tableName);
    //根据表名及时间删除对应数据
    void deleteReportDailyData(@Param("tableName") String tableName,@Param("saveTimes") List<Date> saveTimes);

    void deleteReportCacheData(@Param("tableName") String tableName);

    List<Map<String,Object>> selectReportDropDown();

    //查询资产的品牌
    List<Map<String,Object>> getAssetsBrand(@Param("ids") List<String> ids);

    //查询资产标签位置信息
    List<Map<String,String>> getAssetsLabelLocation(@Param("ids") List<String> ids);
    //查询资产hostId
    List<Map<String,Object>> getAssetsHostId(@Param("ids") List<String> ids);
    //查询蓝月亮数据
    List<LineFlowReportParam> selectLylLinkFlowData(@Param("dateType") Integer dateType,@Param("ids") List<String> ids,@Param("names") List<String> names);
    //查询蓝月亮周期数据
    List<LineFlowReportParam> selectLylLinkReportDailyData(@Param("startTime") Date startTime,@Param("endTime") Date endTime,@Param("ids") List<String> ids,@Param("names") List<String> names);

}

