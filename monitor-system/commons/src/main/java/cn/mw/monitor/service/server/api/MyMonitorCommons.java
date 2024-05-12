package cn.mw.monitor.service.server.api;

import cn.mw.monitor.service.assets.model.RedisItemHistoryDto;
import cn.mw.monitor.service.server.api.dto.ApplicationTableInfos;
import cn.mw.monitor.service.server.api.dto.DateTypeDTO;
import cn.mw.monitor.service.server.api.dto.DiskListDto;
import cn.mw.monitor.service.server.api.dto.HistoryListDto;
import cn.mw.monitor.service.server.api.dto.ItemApplication;
import cn.mw.monitor.service.server.api.dto.LineChartDTO;
import cn.mw.monitor.service.server.api.dto.MWHistoryDTO;
import cn.mw.monitor.service.server.api.dto.QueryApplicationTableParam;
import cn.mwpaas.common.model.Reply;

import java.util.List;
import java.util.Map;

/**
 * @author syt
 * @Date 2021/2/7 16:25
 * @Version 1.0
 */
public interface MyMonitorCommons {
    List<HistoryListDto> getLineChartHistory(LineChartDTO lineChartDTO);

    /**
     * 根据时间段类型，获取需要的参数
     *
     * @param dateType 时间段类型 1==>按一个小时  2==>按一天   3==>按一周   4==>按一个月
     * @return
     */
    DateTypeDTO getDateTypeParams(Integer dateType, String assetsId, String dateStart, String dateEnd);


    /**
     * 根据参数获取峰值的单位和值以及历史数据
     *
     * @param dateTypeDto
     * @param itemsList
     * @param valueType
     * @return
     */
    Map<String, Object> getPeakValue(int monitorServerId, DateTypeDTO dateTypeDto, List<ItemApplication> itemsList, String valueType);

    /**
     * 根据key值获取redis存储的Zsets数据，并转换成list<RedisItemHistoryDto>
     *
     * @param key
     * @return
     */
    List<RedisItemHistoryDto> getRedisItemHistory(String key);

    /**
     * 根据名称精准查询或模糊查询监控项相关最新数据
     *
     * @param monitorServerId 监控服务器id
     * @param hostId          zabbix主机id
     * @param itemNames       监控项名称数组
     * @param flag            是否模糊查询
     * @return
     */
    Reply getItemsIsFilter(int monitorServerId, String hostId, List<String> itemNames, Boolean flag);

    /**
     * 根据Id查询监控项相关最新数据
     *
     * @param monitorServerId 监控服务器id
     * @param hostId          zabbix主机id
     * @param itemIds       监控项Id数组
     * @param flag            是否模糊查询
     * @return
     */
    Reply getItemsIsFilterByItemIds(int monitorServerId, String hostId, List<String> itemIds, Boolean flag);


    /**
     * 根据磁盘名称和磁盘所在的hostId查询有关该磁盘的有关信息
     *
     * @param name
     * @param hostid
     * @return
     */
    DiskListDto getDiskInfoByDiskName(int monitorServerId, String name, String hostid);

    /**
     * 根据监控项id 以及开始结束时间去获取监控项的历史数据
     * @param serverId
     * @param itemId
     * @param time_from  开始时间
     * @param time_till 结束时间
     * @param type  监控项的基本类型必填（不填可能导致数据的获取不到）
     * @return
     */
    List<MWHistoryDTO> getHistoryByItemId(int serverId, String itemId, long time_from, long time_till, Integer type,Boolean isTrend,String valueType,Integer dateType);

    /**
     * 根据应用集，查询应用集下所有监控项生成table表格
     * @param param
     * @return
     */
    ApplicationTableInfos getApplicationTableInfos(QueryApplicationTableParam param);
}
