package cn.mw.monitor.visualized.dao;

import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.visualized.dto.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @ClassName MwVisualizedManageDao
 * @Author gengjb
 * @Date 2022/4/21 15:11
 * @Version 1.0
 **/
public interface MwVisualizedManageDao {

    /**
     * 添加可视化视图分类数据
     * @param visualizedClassifyDto
     * @return
     */
    int  addVisualizedClassify(MwVisualizedClassifyDto visualizedClassifyDto);

    /**
     * 查询可视化分类数据
     * @return
     */
    List<MwVisualizedClassifyDto> selectVisualizedClassify();

    /**
     * 修改可视化视图分类数据
     * @param visualizedClassifyDto
     * @return
     */
    int  updateVisualizedClassify(MwVisualizedClassifyDto visualizedClassifyDto);


    /**
     * 删除可视化视图分类数据
     * @param ids
     * @return
     */
    int deleteVisualizedClassify(@Param("ids") List<Integer> ids);


    /**
     * 添加可视化视图分类数据
     * @param viewDto 视图信息
     * @return
     */
    int  addVisualizedView(MwVisualizedViewDto viewDto);

    /**
     * 修改可视化视图分类数据
     * @param viewDto 视图信息
     * @return
     */
    int  updateVisualizedView(MwVisualizedViewDto viewDto);


    /**
     * 删除可视化视图分类数据
     * @param ids 视图ID集合
     * @return
     */
    int  deleteVisualizedView(@Param("ids") List<Integer> ids);


    /**
     * 查询可视化视图数据
     * @return
     */
    List<MwVisualizedViewDto>  selectVisualizedView(MwVisualizedViewDto viewDto);

    /**
     * 查询所有资产
     * @return
     */
    @Deprecated
    List<MwTangibleassetsTable> selectAllAssets();

    /**
     * 根据资产ID查询数据
     * @param hostIds
     * @return
     */
    @Deprecated
    List<MwTangibleassetsTable> selectAssetsByHostId(@Param("hostIds") List<String> hostIds);

    /**
     * 查询监控项名称
     * @param items
     * @return
     */
    List<Map<String, String>> selectItemName(@Param("items") List<String> items);

    /**
     * 根据ID查询可视化数据
     * @param id 可视化ID
     * @return
     */
    MwVisualizedViewDto selectVisualizedById(@Param("id") Integer id);

    //存储可视化缓存数据
    int visualizedCacheMonitorInfo(@Param("list") List<MwVisualizedCacheDto> cacheDtos);

    //删除缓存数据
    void deleteVisualizedCacheMonitorInfo();

    //根据资产与监控项查询数据
    List<MwVisualizedCacheDto> selectvisualizedCacheInfo(@Param("assetsIds") List<String> assetsIds,@Param("itemName") String itemName);

    //根据资产与多监控项查询
    List<MwVisualizedCacheDto> selectvisualizedCacheInfos(@Param("assetsIds") List<String> assetsIds,@Param("itemNames") List<String> itemNames);

    List<MwVisualizedCacheHistoryDto> selectVisualizedDayData(@Param("assetsIds") List<String> assetsIds,@Param("itemNames") List<String> itemNames,
                                                       @Param("startTime") String startTime,@Param("endTime") String endTime);

    //增加主机和主机组缓存
    int insertHostAndGroupCache(@Param("list") List<MwVisualizedHostGroupDto> visualizedHostGroupDtos);

    //删除主机和主机组缓存
    void deleteHostAndGroupCache();

    //查询主机与主机组信息
    List<MwVisualizedHostGroupDto> selectHostAndGroupCache(@Param("serverName") String serverName,@Param("title") String title);

    //存储可视化历史缓存数据
    int visualizedCacheHistoryMonitorInfo(@Param("list") List<MwVisualizedCacheHistoryDto> cacheDtos);

    void insertVisualizedDayData(@Param("list") List<MwVisualizedCacheHistoryDto> cacheDtos);

    //删除可视化历史缓存数据
    void deleteVisualizedCacheHistoryMonitorInfo();

    //查询可视化历史缓存数据
    List<MwVisualizedCacheHistoryDto> selectVisualizedCacheHistoryMonitorInfo(@Param("assetsId") String assetsId,@Param("itemNames") List<String> itemNames);

    //查询下拉数据
    List<MwVisualizedDropDownDto> selectVisualizedDropDownInfo(@Param("type") Integer type);

    //根据类型查询需要缓存的监控项
    List<String> selectCacheItemByType(@Param("type") Integer type);

    //可视化缓存告警信息
    int visualizedCacheAlertInfo(@Param("list") List<MwVisualizedAlertRecordDto> alertRecordDtos);

    List<MwVisualizedAlertRecordDto> selectAlertCacheInfo(@Param("ids") List<String> assetsIds,@Param("days") List<String> days);

    List<MwVisualizedPrometheusDropDto> selectVisualizedContaine(@Param("typeName") String typeName);

    //可视化添加分区资产数量计数
    int insertVisualizedPartitionAssets(@Param("list") List<MwVisualizedAeestsCountDto> aeestsCountDtos);

    //可视化添加分区资产数量计数
    List<MwVisualizedAeestsCountDto> selectVisualizedPartitionAssets(@Param("name") String name,@Param("days") List<String> times);

    //创建可视化业务状态标题分区信息
    void insertVisualizedBusinStatusTitle(@Param("list") List<MwVisualizedModuleBusinSatusDto> businSatusDtos);

    //查询可视化业务状态标题分区信息
    List<MwVisualizedModuleBusinSatusDto> selectVisualizedBusinStatusTitle(@Param("name") String modelSystemName);

    //查询可视化历史缓存数据批量
    List<MwVisualizedCacheHistoryDto> selectVisualizedCacheHistoryBatch(@Param("assetsIds") List<String> assetsIds,@Param("itemNames") List<String> itemNames);

    //查询容器的url信息
    List<MwVisualizedPrometheusDropDto> selectVisualizedContaineByItemName(@Param("itemName") String itemName);

    //缓存容器告警
    int visualizedCacheContaineAlertInfo(@Param("list")  List<MwVisualizedContainerDto> visualizedContainerDtos);

    //查询容器告警
    List<MwVisualizedContainerDto> getVisualizedCacheContaineAlertInfo(@Param("name") String name,@Param("dates") List<String> dates);

    //获取占比数据
    List<MwVisualizedScoreProportionDto> getVisualizedScoreProportion();

   int saveVisualizedQueryValue(MwVisualizedQueryValueDTO dto);

    MwVisualizedQueryValueDTO getVisualizedQueryValue(Integer id);

    void insertVisualizedImageInfo(@Param("list") List<MwVisualizedImageDto> imageDtos);

    void deleteVisualizedImageInfo(@Param("visualizedId") Integer visualizedId);

    List<MwVisualizedImageDto> selectVisualizedImageInfo(@Param("visualizedId") Integer visualizedId);
}
