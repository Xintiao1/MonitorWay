package cn.mw.monitor.visualized.time;

import cn.mw.monitor.bean.TimeTaskRresult;
import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.assets.param.QueryTangAssetsParam;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.service.server.api.dto.ItemApplication;
import cn.mw.monitor.service.user.api.MWUserCommonService;
import cn.mw.monitor.service.virtual.dto.VirtualizationMonitorInfo;
import cn.mw.monitor.util.IDModelType;
import cn.mw.monitor.util.ModuleIDManager;
import cn.mw.monitor.visualized.constant.RackZabbixItemConstant;
import cn.mw.monitor.visualized.dao.MwVisualizedManageDao;
import cn.mw.monitor.visualized.dto.MwVisualizedCacheDto;
import cn.mw.monitor.visualized.dto.MwVisualizedCacheHistoryDto;
import cn.mw.monitor.visualized.util.MwVisualizedUtil;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @ClassName MwVisualizedModuleTime
 * @Description 可视化定时任务
 * @Author gengjb
 * @Date 2023/5/17 10:00
 * @Version 1.0
 **/
@Component
@ConditionalOnProperty(prefix = "scheduling", name = "enabled", havingValue = "true")
@EnableScheduling
@Slf4j
public class MwVisualizedModuleTime {

    @Autowired
    private MwAssetsManager mwAssetsManager;

    @Autowired
    private MWUserCommonService userService;

    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    @Autowired
    private ModuleIDManager idManager;

    @Resource
    private MwVisualizedManageDao visualizedManageDao;

    @Value("${visualized.group.count}")
    private Integer groupCount;

    @Value("${visualized.host.group}")
    private Integer hostGroupCount;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private MwModelViewCommonService modelViewCommonService;

    private final String rediskey = "VISUALIZED_VDI";

    //缓存资产数据CPU内存等监控项到数据库
//    @Scheduled(cron = "0 0/5 * * * ?")
    public TimeTaskRresult visualizedCacheInfo(){
        log.info(">>>>>>>MwVisualizedModuleTime>>>>>>start");
        TimeTaskRresult result = new TimeTaskRresult();
        try {
            //获取资产
            List<MwTangibleassetsTable> tangibleassetsTables = getAssetsInfo();
            if(CollectionUtils.isEmpty(tangibleassetsTables)){return null;}
            //分组
            Map<Integer, List<String>> groupMap = tangibleassetsTables.stream().filter(item->item.getMonitorServerId() != null &&  item.getMonitorServerId() != 0)
                    .collect(Collectors.groupingBy(MwTangibleassetsTable::getMonitorServerId, Collectors.mapping(MwTangibleassetsTable::getAssetsId, Collectors.toList())));
            Map<String,MwTangibleassetsTable> assetsMap = new HashMap<>();
            tangibleassetsTables.forEach(item->{
                assetsMap.put(item.getMonitorServerId()+item.getAssetsId(),item);
            });
            //查询zabbix监控项
            List<MwVisualizedCacheDto> visualizedCacheDtos = getZabbixItemInfo(groupMap, assetsMap, 1);
            getInterFaceInfo(tangibleassetsTables,visualizedCacheDtos);
            if(CollectionUtils.isEmpty(visualizedCacheDtos)){return null;}
            //先删除原先缓存数据
            visualizedManageDao.deleteVisualizedCacheMonitorInfo();
            List<List<MwVisualizedCacheDto>> subLists = Lists.partition(visualizedCacheDtos, groupCount);
            for (List<MwVisualizedCacheDto> cacheDtos : subLists) {
                visualizedManageDao.visualizedCacheMonitorInfo(cacheDtos);
            }
            result.setSuccess(true);
            result.setResultType(0);
            result.setResultContext("缓存可视化监控项数据:成功");
        }catch (Throwable e){
            log.error("MwVisualizedModuleTime{} visualizedCacheInfo::",e);
            result.setSuccess(false);
            result.setResultType(0);
            result.setResultContext("缓存可视化监控项数据:失败");
            result.setFailReason(e.getMessage());
        }
        log.info(">>>>>>>MwVisualizedModuleTime>>>>>>end");
        return result;
    }

    /**
     * 获取资产信息
     */
    private List<MwTangibleassetsTable> getAssetsInfo(){
        QueryTangAssetsParam assetsParam = new QueryTangAssetsParam();
        assetsParam.setPageNumber(1);
        assetsParam.setPageSize(Integer.MAX_VALUE);
        assetsParam.setIsQueryAssetsState(false);
        assetsParam.setUserId(userService.getAdmin());
        return mwAssetsManager.getAssetsTable(assetsParam);
    }

    /**
     * 获取zabbix监控项数据
     * @param groupMap
     */
    private List<MwVisualizedCacheDto> getZabbixItemInfo( Map<Integer, List<String>> groupMap,Map<String,MwTangibleassetsTable> assetsMap,Integer itemType){
        List<MwVisualizedCacheDto> realDatas = new ArrayList<>();
        Map<String, List<MwVisualizedCacheDto>>  cacheMap = new HashMap<>();
        List<String> itemNames = visualizedManageDao.selectCacheItemByType(itemType);
        log.info("TimeTaskRresult{} getZabbixItemInfo::"+itemNames);
        if(CollectionUtils.isEmpty(itemNames)){return realDatas;}
        for (Map.Entry<Integer, List<String>> entry : groupMap.entrySet()) {
            Integer serverId = entry.getKey();
            List<String> hosts = entry.getValue();
            //hostId分组查询
            List<List<String>> partition = Lists.partition(hosts, hostGroupCount);
            for (List<String> hostIds : partition) {
                log.info("MwVisualizedModuleTime {} getZabbixItemInfo() hostIds::"+hostIds.size());
                //查询监控项数据
                MWZabbixAPIResult result = mwtpServerAPI.itemGetbySearch(serverId, itemNames, hostIds);
                if(result == null || result.isFail()){continue;}
                boolean validJSON = MwVisualizedUtil.isValidJSON(JSON.toJSONString(String.valueOf(result.getData())));
                if(!validJSON){continue;}
                List<ItemApplication> itemApplications = JSONArray.parseArray(String.valueOf(result.getData()), ItemApplication.class);
                if(CollectionUtils.isEmpty(itemApplications)){continue;}
                //根据主机进行数据分组
                Map<String, List<ItemApplication>> hostMap = itemApplications.stream().collect(Collectors.groupingBy(item -> item.getHostid()));
                for (String hostId : hostMap.keySet()) {
                    List<ItemApplication> items = hostMap.get(hostId);
                    if(CollectionUtils.isEmpty(items)){continue;}
                    //根据名称分组
                    Map<String, List<ItemApplication>> itemNameApplications = items.stream().collect(Collectors.groupingBy(item -> item.getName()));
                    MwTangibleassetsTable tangibleassetsTable = assetsMap.get(serverId + hostId);
                    for (String itemName : itemNameApplications.keySet()) {
                        List<String> filterItem = RackZabbixItemConstant.FILTER_ITEM;
                        if(filterItem.contains(itemName)){continue;}
                        List<ItemApplication> applications = itemNameApplications.get(itemName);
                        MwVisualizedCacheDto cacheDto = new MwVisualizedCacheDto();
                        if(StringUtils.isEmpty(applications.get(0).getLastvalue())){continue;}
                        cacheDto.extractFrom(applications.get(0),tangibleassetsTable);
                        cacheDto.setCacheId(String.valueOf(idManager.getID(IDModelType.Visualized)));
                        if(cacheMap.containsKey(itemName)){
                            List<MwVisualizedCacheDto> mwVisualizedCacheDtos = cacheMap.get(itemName);
                            mwVisualizedCacheDtos.add(cacheDto);
                            cacheMap.put(itemName,mwVisualizedCacheDtos);
                        }else{
                            List<MwVisualizedCacheDto> mwVisualizedCacheDtos = new ArrayList<>();
                            mwVisualizedCacheDtos.add(cacheDto);
                            cacheMap.put(itemName,mwVisualizedCacheDtos);
                        }
                    }
                }
            }
        }
        for (Map.Entry<String, List<MwVisualizedCacheDto>> entry : cacheMap.entrySet()) {
            List<MwVisualizedCacheDto> cacheDtos = entry.getValue();
            if(CollectionUtils.isEmpty(cacheDtos)){continue;}
            realDatas.addAll(cacheDtos);
        }
        return realDatas;
    }


    private final String FILTER_NAME = "核心交换机";

    /**
     * 是否有需要筛选的数据
     * @param tangibleassetsTables
     */
    private void getInterFaceInfo(List<MwTangibleassetsTable> tangibleassetsTables,List<MwVisualizedCacheDto> visualizedCacheDtos){
        List<MwTangibleassetsTable> tables = tangibleassetsTables.stream().filter(item -> StringUtils.isNotBlank(item.getModelClassify()) && item.getModelClassify().equals(FILTER_NAME)).collect(Collectors.toList());
        log.info("MwVisualizedModuleTime{} getInterFaceInfo() tables::"+tables.size());
        if(CollectionUtils.isEmpty(tables)){return;}
        //分组
        Map<Integer, List<String>> groupMap = tables.stream().filter(item->item.getMonitorServerId() != null &&  item.getMonitorServerId() != 0)
                .collect(Collectors.groupingBy(MwTangibleassetsTable::getMonitorServerId, Collectors.mapping(MwTangibleassetsTable::getAssetsId, Collectors.toList())));
        Map<String,MwTangibleassetsTable> assetsMap = new HashMap<>();
        tables.forEach(item->{
            assetsMap.put(item.getMonitorServerId()+item.getAssetsId(),item);
        });
        List<MwVisualizedCacheDto> cacheDtos = getZabbixItemInfo(groupMap, assetsMap, 3);
        visualizedCacheDtos.addAll(cacheDtos);
    }



    /**
     * 可视化vdi数据存储redis
     * @return
     */
//    @Scheduled(cron = "0 0/5 * * * ?")
    public TimeTaskRresult visualizedVdiCache(){
        TimeTaskRresult result = new TimeTaskRresult();
        try {
            log.info(">>>>>MwVisualizedModuleTime>>>>visualizedVdiCache>>>start");
            List<VirtualizationMonitorInfo> allVirtualInfoByMonitorData = modelViewCommonService.getAllVirtualInfoByMonitorData();
            redisTemplate.delete(rediskey);
            redisTemplate.opsForValue().set(rediskey, JSONObject.toJSONString(allVirtualInfoByMonitorData), 10, TimeUnit.MINUTES);
            result.setSuccess(true);
            result.setResultType(0);
            result.setResultContext("缓存可视化监控项数据:成功");
            log.info(">>>>>MwVisualizedModuleTime>>>>visualizedVdiCache>>>end");
        }catch (Throwable e){
            log.error("MwVisualizedModuleTime{} visualizedVdiCache::",e);
            result.setSuccess(false);
            result.setResultType(0);
            result.setResultContext("缓存可视化监控项数据:失败");
            result.setFailReason(e.getMessage());
        }

        return result;
    }
}
