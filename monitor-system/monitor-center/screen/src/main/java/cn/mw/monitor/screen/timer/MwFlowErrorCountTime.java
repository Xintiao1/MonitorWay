package cn.mw.monitor.screen.timer;

import cn.mw.monitor.bean.TimeTaskRresult;
import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.screen.dto.ItemNameRank;
import cn.mw.monitor.screen.dto.ItemRank;
import cn.mw.monitor.screen.dto.TitleRank;
import cn.mw.monitor.screen.service.GetDataByCallable;
import cn.mw.monitor.service.alert.dto.AssetsDto;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.assets.param.QueryTangAssetsParam;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.service.user.api.MWUserCommonService;
import cn.mw.monitor.util.UnitsUtil;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.utils.CollectionUtils;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @ClassName MwFlowErrorCountTime
 * @Description 定时缓存流量错误包信息到redis
 * @Author gengjb
 * @Date 2022/9/20 10:32
 * @Version 1.0
 **/
@Component
@Slf4j(topic = "timerController")
public class MwFlowErrorCountTime {

    @Autowired
    private MwModelViewCommonService mwModelViewCommonService;

    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private MwAssetsManager assetsManager;

    @Autowired
    private MWUserCommonService commonService;

    @Value("${screen.timetask}")
    private boolean isExecuteTimeTask;

    /**
     * 获取流量错误包数据
     */
//    @Scheduled(cron = "0 0/5 * * * ?")
    public TimeTaskRresult getFlowErrorCount(){
        if(!isExecuteTimeTask){return null;}
        List<String> itemNames = new ArrayList<>();
        itemNames.add("INTERFACE_OUT_ERRORS");
        itemNames.add("INTERFACE_IN_ERRORS");
        for (String itemName : itemNames){
            ItemRank hostRankCacheData = getHostRankCacheData(itemName);
            //先删除原来的缓存
            redisTemplate.delete("homePage" + itemName);
            //再将新的数据存入缓存
            redisTemplate.opsForValue().set("homePage"+itemName, JSONObject.toJSONString(hostRankCacheData), 60, TimeUnit.MINUTES);
            log.info("缓存首页流量错误包数据成功");
        }
        TimeTaskRresult taskRresult = new TimeTaskRresult();
        //进行数据添加
        taskRresult.setSuccess(true);
        taskRresult.setResultType(0);
        taskRresult.setResultContext("缓存流量错误包数据成功");
        return taskRresult;
    }

    public ItemRank getHostRankCacheData(String name) {
        ItemRank itemRank = new ItemRank();
        //获取资产Id信息
        Map<Integer, List<String>> map = getAssetsIds();
        if (null != map && map.size() > 0) {
            for (Integer key : map.keySet()) {
                List<String> hostIds = map.get(key);
                MWZabbixAPIResult result = mwtpServerAPI.itemGetbyType(key, name, hostIds);
                if (result != null && result.code == 0) {
                    JsonNode itemData = (JsonNode) result.getData();
                    if (itemData.size() > 0) {
                        List<ItemNameRank> itemNameRanks = new ArrayList<>();
                        List<TitleRank> titleRanks = new ArrayList<>();
                        TitleRank titleRank1 = TitleRank.builder().name("资产名称").fieldName("name").build();
                        TitleRank titleRank2 = TitleRank.builder().name("IP地址").fieldName("ip").build();
                        titleRanks.add(titleRank1);
//                        titleRanks.add(titleRank2);
                        ExecutorService executorService = Executors.newFixedThreadPool(40);
                        List<Future<ItemNameRank>> futureList = new ArrayList<>();
                        for (JsonNode item : itemData) {
                            GetDataByCallable<ItemNameRank> getDataByCallable = new GetDataByCallable<ItemNameRank>() {
                                @Override
                                public ItemNameRank call() throws Exception {
                                    ItemNameRank itemNameRank = new ItemNameRank();
                                    String hostId = item.get("hostid").asText();
                                    AssetsDto assets = mwModelViewCommonService.getAssetsById(hostId ,key);
                                    if (null != assets) {
                                        itemNameRank.setId(assets.getId());
                                        itemNameRank.setName(assets.getAssetsName());
                                        itemNameRank.setIp(assets.getAssetsIp());
                                        itemNameRank.setAssetsId(assets.getAssetsId());
                                        itemNameRank.setMonitorServerId(assets.getMonitorServerId());

                                        String lastvalue = item.get("lastvalue").asText();
                                        Map<String, String> map1 = UnitsUtil.getValueAndUnits(lastvalue, item.get("units").asText());
                                        Map<String, String> map2 = UnitsUtil.getConvertedValue(new BigDecimal(lastvalue), item.get("units").asText());
                                        if (null != map2) {
                                            String dataUnits = map2.get("units");
                                            String v = UnitsUtil.getValueMap(lastvalue, dataUnits, item.get("units").asText()).get("value");
                                            Double values = new BigDecimal(v).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                                            itemNameRank.setSortlastValue(Double.valueOf(lastvalue));
                                            itemNameRank.setLastValue(values);
                                            itemNameRank.setProgress(values);
                                            itemNameRank.setUnits(dataUnits);
                                        }
                                        if (name.equals("DISK_UTILIZATION") ) {
                                            int i = item.get("name").asText().indexOf("]");
                                            if (i != -1) {
                                                itemNameRank.setType(item.get("name").asText().substring(1, i));
                                                itemNameRank.setName(assets.getAssetsName() + item.get("name").asText().substring(0, i + 1));
                                            }
                                        }
                                        if (name.equals("INTERFACE_IN_TRAFFIC") || name.equals("INTERFACE_OUT_TRAFFIC") || name.equals("INTERFACE_IN_UTILIZATION")
                                                || name.equals("INTERFACE_OUT_UTILIZATION") || name.equals("INTERFACE_OUT_ERRORS") || name.equals("INTERFACE_IN_ERRORS")) {
                                            int i = item.get("name").asText().indexOf("]");
                                            if (i != -1) {
                                                itemNameRank.setType(item.get("name").asText().substring(1, i));
                                                itemNameRank.setName(assets.getAssetsName());
                                            }
                                        }
//                                        itemNameRanks.add(itemNameRank);
                                    }
                                    return itemNameRank;
                                }
                            };
                            if(null!=getDataByCallable){
                                Future<ItemNameRank> f = executorService.submit(getDataByCallable);
                                futureList.add(f);
                            }
                        }
                        for (Future<ItemNameRank> itemNameRankFuture : futureList) {
                            try {
                                ItemNameRank itemNameRank = itemNameRankFuture.get(30, TimeUnit.SECONDS);
                                if(itemNameRank.getSortlastValue()>=0){
                                    itemNameRanks.add(itemNameRank);
                                }
                            } catch (Exception e) {
                                itemNameRankFuture.cancel(true);
                                executorService.shutdown();
                            }
                        }
                        executorService.shutdown();
                        //一个资产有多个cpu或者内存取最大一个
                        if("CPU_UTILIZATION".equals(name) || "MEMORY_UTILIZATION".equals(name)){
                            Map<String, List<ItemNameRank>> collect = itemNameRanks.stream().collect(Collectors.groupingBy(ItemNameRank::getAssetsId));
                            ItemNameRank ite=new ItemNameRank();
                            List<ItemNameRank> list=new ArrayList<>();
                            for (List<ItemNameRank> ranks : collect.values()) {
                                if(ranks.size()>1){
                                    Collections.sort(ranks, new ItemNameRank());
                                    ite = ranks.get(0);
                                }else {
                                    ite =ranks.get(0);
                                }
                                list.add(ite);
                            }
                            itemNameRanks=list;
                        }
                        if(!name.equals("INTERFACE_OUT_ERRORS") && !name.equals("INTERFACE_IN_ERRORS")){
                            Collections.sort(itemNameRanks, new ItemNameRank());//倒序排序
                            if (itemNameRanks.size() > 20) {
                                itemNameRanks = itemNameRanks.subList(0, 20);
                            }
                        }
                        List<String> node = new ArrayList<>();
                        node.add("资产名称");
                        itemRank.setTitleNode(node);
                        List<ItemNameRank> itemNameRankList = itemRank.getItemNameRankList();
                        if(CollectionUtils.isNotEmpty(itemNameRankList)){
                            itemNameRankList.addAll(itemNameRanks);
                            itemRank.setItemNameRankList(itemNameRankList);
                        }else{
                            itemRank.setItemNameRankList(itemNameRanks);
                        }
                        List<TitleRank> titleRanks2 = itemRank.getTitleRanks();
                        if(CollectionUtils.isNotEmpty(titleRanks2)){
                            titleRanks2.addAll(titleRanks);
                            itemRank.setTitleRanks(titleRanks2);
                        }else{
                            itemRank.setTitleRanks(titleRanks);
                        }
                    }
                }
            }
        }
        return itemRank;
    }


    private Map<Integer,List<String>> getAssetsIds(){
        QueryTangAssetsParam queryTangAssetsParam = new QueryTangAssetsParam();
        queryTangAssetsParam.setPageNumber(1);
        queryTangAssetsParam.setPageSize(Integer.MAX_VALUE);
        queryTangAssetsParam.setUserId(commonService.getAdmin());
        queryTangAssetsParam.setSkipDataPermission(true);
        List<MwTangibleassetsTable> assetsTable = assetsManager.getAssetsTable(queryTangAssetsParam);
        Map<String, Object> map = new HashMap<>();
        map.put("assetsList",assetsTable);
        return assetsManager.getAssetsByServerId(map);
    }
}
