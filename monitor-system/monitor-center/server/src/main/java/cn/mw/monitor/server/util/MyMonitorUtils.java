package cn.mw.monitor.server.util;


import cn.mw.monitor.common.constant.ZabbixItemConstant;
import cn.mw.monitor.server.serverdto.ItemGetDTO;
import cn.mw.monitor.server.service.MwValueTypeEnum;
import cn.mw.monitor.server.service.impl.MwServerManager;
import cn.mw.monitor.service.assets.model.RedisItemHistoryDto;
import cn.mw.monitor.service.server.api.MyMonitorCommons;
import cn.mw.monitor.service.server.api.dto.ApplicationTableInfos;
import cn.mw.monitor.service.server.api.dto.ColTable;
import cn.mw.monitor.service.server.api.dto.DateTypeDTO;
import cn.mw.monitor.service.server.api.dto.DiskListDto;
import cn.mw.monitor.service.server.api.dto.HistoryListDto;
import cn.mw.monitor.service.server.api.dto.ItemApplication;
import cn.mw.monitor.service.server.api.dto.LineChartDTO;
import cn.mw.monitor.service.server.api.dto.MWHistoryDTO;
import cn.mw.monitor.service.server.api.dto.MWItemHistoryDto;
import cn.mw.monitor.service.server.api.dto.QueryApplicationTableParam;
import cn.mw.monitor.service.webmonitor.model.MwHistoryDTO;
import cn.mw.monitor.util.SeverityUtils;
import cn.mw.monitor.util.UnitsUtil;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mw.zbx.manger.MWWebZabbixManger;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.DateUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author syt
 * @Date 2021/2/5 11:07
 * @Version 1.0
 */
@Component
@Slf4j
public class MyMonitorUtils implements MyMonitorCommons {

    @Resource
    private MWWebZabbixManger mwWebZabbixManger;

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private MWTPServerAPI mwtpServerAPI;
    @Autowired
    private MwServerManager mwServerManager;

    @Value("${server.group.count}")
    private Integer groupCount;

    /**
     * 根据条件获取不同时间段处理过的历史数据
     *
     * @param lineChartDTO
     * @return
     */
    @Override
    public List<HistoryListDto> getLineChartHistory(LineChartDTO lineChartDTO) {
        List<HistoryListDto> lists = new ArrayList<>();
        List<ItemApplication> list = lineChartDTO.getItemApplicationList();
        if (list.size() > 0) {
//            当list不为空时有意义
            DateTypeDTO dateTypeParams = getDateTypeParams(lineChartDTO.getDateType(), lineChartDTO.getAssetsBaseDTO().getId(), lineChartDTO.getDateStart(), lineChartDTO.getDateEnd());
//            获取峰值单位以及未处理的历史数据
            Map<String, Object> map = getPeakValue(lineChartDTO.getAssetsBaseDTO().getMonitorServerId(), dateTypeParams, list, lineChartDTO.getValueType());
//            对数据进行处理
            for (ItemApplication item : list) {
                HistoryListDto dto = new HistoryListDto();
                List<MwHistoryDTO> historyDTOList = new ArrayList<>();

                if (!dateTypeParams.getFlag()) {
                    List<MWItemHistoryDto> MWItemHistoryDtos = (List<MWItemHistoryDto>) map.get(item.getItemid());
                    if (MWItemHistoryDtos.size() > 0) {
                        MWItemHistoryDtos.forEach(
                                respDto -> {
                                    historyDTOList.add(MwHistoryDTO.builder()
                                            .value(UnitsUtil.getValueMap(respDto.getValue(), (String) map.get("units"), item.getUnits()).get("value"))
                                            .dateTime(new Date(Long.valueOf(respDto.getClock()) * 1000L)).build());
                                }
                        );
                    }
                } else {
                    List<RedisItemHistoryDto> redisItemHistoryDtos = (List<RedisItemHistoryDto>) map.get(item.getItemid());
                    if (redisItemHistoryDtos.size() > 0) {
                        for (RedisItemHistoryDto respDto : redisItemHistoryDtos) {
                            String value = "";
                            if ("AVG".equals(lineChartDTO.getValueType())) {
                                value = respDto.getAvgValue();
                            } else if ("MAX".equals(lineChartDTO.getValueType())) {
                                value = respDto.getMaxValue();
                            } else if ("MIN".equals(lineChartDTO.getValueType())) {
                                value = respDto.getMinValue();
                            }
                            respDto.setValue(value);
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            try {
                                historyDTOList.add(MwHistoryDTO.builder()
                                        .value(UnitsUtil.getValueMap(respDto.getValue(), (String) map.get("units"), item.getUnits()).get("value"))
                                        .dateTime(simpleDateFormat.parse(respDto.getUpdateTime())).build());
                            } catch (ParseException e) {
                                log.error("ParseException", e);
                            }
                        }
                    }
                }
                if (historyDTOList.size() > 0) {
                    //最后一条数据的值
                    BigDecimal lastValue = new BigDecimal(historyDTOList.get(historyDTOList.size() - 1).getValue());
                    dto.setLastUpdateValue(lastValue.toString());
                }
                dto.setUnit((String) map.get("units"));
                dto.setTitleName(item.getChName());
                dto.setDataList(historyDTOList);
                dto.setLastUpdateTime(SeverityUtils.getDate(new Date()));
                lists.add(dto);
            }
        }
        return lists;
    }

    /**
     * 根据时间段类型，获取需要的参数
     *
     * @param dateType 时间段类型 1==>按一个小时  2==>按一天   3==>按一周   4==>按一个月
     * @return
     */
    @Override
    public DateTypeDTO getDateTypeParams(Integer dateType, String assetsId, String dateStart, String dateEnd) {
        DateTypeDTO dateTypeDto = new DateTypeDTO();
        Calendar calendar = Calendar.getInstance();
        dateTypeDto.setFlag(false);
        switch (dateType) {
            case 1:
                //            按一个小时
                dateTypeDto.setEndTime(calendar.getTimeInMillis() / 1000L);
                calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) - 1);
                dateTypeDto.setStartTime(calendar.getTimeInMillis() / 1000L);
                return dateTypeDto;
            case 2:
                //            按一天
                dateTypeDto.setEndTime(calendar.getTimeInMillis() / 1000L);
                calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - 1);
                dateTypeDto.setStartTime(calendar.getTimeInMillis() / 1000L);
                return dateTypeDto;
            case 3:
                //            按一周
                dateTypeDto.setFlag(true);
                dateTypeDto.setKeyPrefix(assetsId + ":15mins:");
                return dateTypeDto;
            case 4:
                //            按一个月
                dateTypeDto.setFlag(true);
                dateTypeDto.setKeyPrefix(assetsId + ":60mins:");
                return dateTypeDto;
            case 5:
                //           自定义
                dateTypeDto.setStartTime(DateUtils.parse(dateStart).getTime() / 1000L);
                dateTypeDto.setEndTime(DateUtils.parse(dateEnd).getTime() / 1000L);
                return dateTypeDto;
            default:
                break;

        }
        return dateTypeDto;
    }


    /**
     * 根据参数获取峰值的单位和值以及历史数据
     *
     * @param dateTypeDto
     * @param itemsList
     * @param valueType
     * @return
     */
    @Override
    public Map<String, Object> getPeakValue(int monitorServerId, DateTypeDTO dateTypeDto, List<ItemApplication> itemsList, String valueType) {
        Map<String, Object> map = new HashMap<>();
        if (itemsList.size() > 0) {
//        找数据最大值
            Double maxValue = 0.0;
            for (ItemApplication item : itemsList) {
                if (!dateTypeDto.getFlag()) {
//              一个小时和一天的情况下的数据获取
                    List<MWItemHistoryDto> list = mwWebZabbixManger.HistoryGetByTimeAndHistory(monitorServerId, item.getItemid(), dateTypeDto.getStartTime(), dateTypeDto.getEndTime(), Integer.valueOf(item.getValue_type()));
                    map.put(item.getItemid(), list);
//                    判断最大值
                    if (maxValue < getMaxValue(list).doubleValue()) {
                        maxValue = getMaxValue(list).doubleValue();
                    }
                } else {
//              一周和一月的情况下的数据获取
                    String key = dateTypeDto.getKeyPrefix() + item.getItemid() + item.getName();
                    List<RedisItemHistoryDto> list = getRedisItemHistory(key);
                    map.put(item.getItemid(), list);
//              以数组最后一个元素作为判定条件
                    if (list.size() > 0) {
//                            通过前端传来的valueType参数，判断获取的是哪一个的最大值
                        if ("AVG".equals(valueType) && list.get(list.size() - 1).getAvgMax() > maxValue) {
                            maxValue = list.get(0).getAvgMax();
                        } else if ("MAX".equals(valueType) && list.get(list.size() - 1).getMaxMax() > maxValue) {
                            maxValue = list.get(0).getMaxMax();
                        } else if ("MIN".equals(valueType) && list.get(list.size() - 1).getMinMax() > maxValue) {
                            maxValue = list.get(0).getMinMax();
                        }
                    }
                }
            }
            Map<String, String> valueAndUnits = UnitsUtil.getValueAndUnits(maxValue.toString(), itemsList.get(0).getUnits());
            map.putAll(valueAndUnits);
        }
        return map;
    }

    /**
     * 根据key值获取redis存储的Zsets数据，并转换成list<RedisItemHistoryDto>
     *
     * @param key
     * @return
     */
    @Override
    public List<RedisItemHistoryDto> getRedisItemHistory(String key) {
        Set<String> range = redisTemplate.opsForZSet().range(key, 0, -1);
        List<RedisItemHistoryDto> list = new ArrayList<>();
        if (!range.isEmpty()) {
            for (String str : range) {
                if (null != str && StringUtils.isNotEmpty(str) && !"null".equals(str)) {
                    RedisItemHistoryDto redisItemHistoryDto = JSONObject.parseObject(str, RedisItemHistoryDto.class);
                    list.add(redisItemHistoryDto);
                }
            }
        }
        list = list.stream().filter(dto -> dto.getUpdateTime() != null).collect(Collectors.toList());
        return list;
    }

    //根据lastValue找到list中的最大值
    public static Long getMaxValue(List<MWItemHistoryDto> list) {
        Long max = 0L;
        for (int i = 0; i < list.size(); i++) {
            Long lastValue = list.get(i).getLastValue();
            if (max < lastValue) {
                max = lastValue;
            }
        }
        return max;
    }

    /**
     * 根据名称精准查询或模糊查询监控项相关最新数据
     *
     * @param monitorServerId
     * @param hostId
     * @param itemNames
     * @param flag
     * @return
     */
    public Reply getItemsIsFilter(int monitorServerId, String hostId, List<String> itemNames, Boolean flag) {
        Map<String, Object> map = new HashMap<>();
        List<ItemApplication> list = new ArrayList<>();
        MWZabbixAPIResult result;
        List<List<String>> partition = Lists.partition(itemNames, groupCount);
        for (List<String> items : partition) {
            if (!flag) {
                result = mwtpServerAPI.itemGetbyFilter(monitorServerId, items, hostId);
            } else {
                result = mwtpServerAPI.itemGetbySearch(monitorServerId, items, hostId);
            }
            if (!result.isFail()){
                String data = String.valueOf(result.getData());
                list.addAll(JSONArray.parseArray(data, ItemApplication.class));
            }
        }
        boolean typeFlag = false;
        if (CollectionUtils.isNotEmpty(list)) {
            for (ItemApplication item : list) {
                //查找中文名称
                item.setChName(mwServerManager.getChNameWithout(item.getName()));
                if (item.getName() != null && item.getName().indexOf("[") != -1) {
                    item.setTypeName(item.getName().substring(item.getName().indexOf("[") + 1, item.getName().indexOf("]")));
                } else {
                    typeFlag = true;
                }
                String newValue = "";
                if (!"0".equals(item.getValuemapid())) {
                    newValue = mwServerManager.getValueMapById(monitorServerId, item.getValuemapid(), item.getLastvalue());
                } else {
                    if ("uptime".equals(item.getUnits())) {
                        double v = Double.parseDouble(item.getLastvalue());
                        long l = new Double(v).longValue();
                        newValue = SeverityUtils.getLastTime(l);
                    } else {
                        if ("0".equals(item.getValue_type()) || "3".equals(item.getValue_type())) {
                            newValue = UnitsUtil.getValueWithUnits(item.getLastvalue(), item.getUnits());
                            item.setDoubleValue(new BigDecimal(item.getLastvalue()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                        } else {
                            newValue = item.getLastvalue();
                        }
                    }
                }
                item.setLastvalue(newValue);
            }
        }
        if (flag) {
            if (!typeFlag) {
                Map<String, List<ItemApplication>> typeListMap = list.stream().collect(Collectors.groupingBy(ItemApplication::getTypeName));
                map.put("typeItemNames", typeListMap);
            }
            Map<String, List<ItemApplication>> itemListMap = list.stream().collect(Collectors.groupingBy(ItemApplication::getChName));
            map.put("itemList", itemListMap);
            list.forEach(l -> {
                if (l.getTypeName() != null && !"".equals(l.getTypeName())) {
                    l.setChName("[" + l.getTypeName() + "]" + l.getChName());
                }
            });
        }
        setListOrder(itemNames,list);
        map.put("list", list);
        return Reply.ok(map);
    }

    /**
     *  按照 orderRegulation 里的 顺序 来排序 targetList
     * @param orderRegulation
     * @param targetList
     */
    public static void setListOrder(List<String> orderRegulation, List<ItemApplication> targetList) {
        Collections.sort(targetList, ((o1, o2) -> {
            int io1 = orderRegulation.indexOf(o1.getName());
            int io2 = orderRegulation.indexOf(o2.getName());

            if (io1 != -1) {
                io1 = targetList.size() - io1;
            }
            if (io2 != -1) {
                io2 = targetList.size() - io2;
            }

            return io2 - io1;
        }));
    }

    /**
     * 根据Id查询监控项相关最新数据
     *
     * @param monitorServerId
     * @param hostId
     * @param itemIds
     * @param flag
     * @return
     */
    public Reply getItemsIsFilterByItemIds(int monitorServerId, String hostId, List<String> itemIds, Boolean flag) {
        Map<String, Object> map = new HashMap<>();
        List<ItemApplication> list = new ArrayList<>();
        MWZabbixAPIResult result = mwtpServerAPI.getItemName(monitorServerId, itemIds);
        boolean typeFlag = false;
        if (!result.isFail()) {
            String data = String.valueOf(result.getData());
            list = JSONArray.parseArray(data, ItemApplication.class);

            for (ItemApplication item : list) {
                //查找中文名称
                item.setChName(mwServerManager.getChNameWithout(item.getName()));
                if (item.getName() != null && item.getName().indexOf("[") != -1) {
                    item.setTypeName(item.getName().substring(item.getName().indexOf("[") + 1, item.getName().indexOf("]")));
                } else {
                    typeFlag = true;
                }
                String newValue = "";
                if (!"0".equals(item.getValuemapid())) {
                    newValue = mwServerManager.getValueMapById(monitorServerId, item.getValuemapid(), item.getLastvalue());
                } else {
                    if ("uptime".equals(item.getUnits())) {
                        double v = Double.parseDouble(item.getLastvalue());
                        long l = new Double(v).longValue();
                        newValue = SeverityUtils.getLastTime(l);
                    } else {
                        if ("0".equals(item.getValue_type()) || "3".equals(item.getValue_type())) {
                            newValue = UnitsUtil.getValueWithUnits(item.getLastvalue(), item.getUnits());
                            item.setDoubleValue(new BigDecimal(item.getLastvalue()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                        } else {
                            newValue = item.getLastvalue();
                        }
                    }
                }
                item.setLastvalue(newValue);
            }
        }
        if (flag) {
            if (!typeFlag) {
                Map<String, List<ItemApplication>> typeListMap = list.stream().collect(Collectors.groupingBy(ItemApplication::getTypeName));
                map.put("typeItemNames", typeListMap);
            }
            Map<String, List<ItemApplication>> itemListMap = list.stream().collect(Collectors.groupingBy(ItemApplication::getChName));
            map.put("itemList", itemListMap);
            list.forEach(l -> {
                if (l.getTypeName() != null && !"".equals(l.getTypeName())) {
                    l.setChName("[" + l.getTypeName() + "]" + l.getChName());
                }
            });
        }
        map.put("list", list);
        return Reply.ok(map);
    }


    /**
     * 根据磁盘名称和磁盘所在的hostId查询有关该磁盘的有关信息
     *
     * @param name
     * @param hostid
     * @return
     */
    public DiskListDto getDiskInfoByDiskName(int monitorServerId, String name, String hostid) {
        DiskListDto diskListDto = new DiskListDto();
        diskListDto.setType(name);
        String newName = "[" + name + "]" + "MW_";
        MWZabbixAPIResult result1 = mwtpServerAPI.itemGetbyType(monitorServerId, newName, hostid, false);
        if (result1.getCode() == 0) {
            JsonNode node = (JsonNode) result1.getData();
            if (node.size() > 0) {
                node.forEach(data -> {
                    String dataName = data.get("name").asText();
                    String lastValue = data.get("lastvalue").asText();
                    String units = data.get("units").asText();
                    lastValue = UnitsUtil.getValueWithUnits(lastValue, units);
                    if (dataName.equals(newName + ZabbixItemConstant.diskItemName.get(0))) {
                        diskListDto.setDiskUserRate(lastValue);
                    } else if (dataName.equals(newName + ZabbixItemConstant.diskItemName.get(1))) {
                        diskListDto.setDiskUser(lastValue);
                    } else if (dataName.equals(newName + ZabbixItemConstant.diskItemName.get(2))) {
                        diskListDto.setDiskFree(lastValue);
                    } else if (dataName.equals(newName + ZabbixItemConstant.diskItemName.get(3))) {
                        diskListDto.setDiskTotal(lastValue);
                    }
                });
            }
        }
        return diskListDto;
    }

    @Override
    public List<MWHistoryDTO> getHistoryByItemId(int serverId, String itemId, long time_from, long time_till, Integer type,Boolean isTrend,String valueType,Integer dateType) {
        MWZabbixAPIResult mwZabbixAPIResult;
        if(isTrend != null && !isTrend && dateType != 1){
            mwZabbixAPIResult = mwtpServerAPI.HistoryGetByTimeAndType(serverId, itemId, time_from, time_till, type);
        }else{
            mwZabbixAPIResult = mwtpServerAPI.trendBatchGet(serverId,Arrays.asList(new String[]{itemId}), time_from, time_till);
        }
        String data = String.valueOf(mwZabbixAPIResult.getData());
        List<MWHistoryDTO> list = JSONArray.parseArray(data, MWHistoryDTO.class);
        handleHistoryDto(list,isTrend,valueType);
        return list;
    }

    private void handleHistoryDto(List<MWHistoryDTO> list,Boolean isTrend,String valueType){
        if(CollectionUtils.isEmpty(list) || (isTrend != null && !isTrend))return;
        for (MWHistoryDTO mwHistoryDTO : list) {
            if(valueType.equals(String.valueOf(MwValueTypeEnum.AVG))){
                mwHistoryDTO.setValue(String.valueOf(mwHistoryDTO.getValueAvg()));
                mwHistoryDTO.setLastValue(mwHistoryDTO.getValueAvg());
            }
            if(valueType.equals(String.valueOf(MwValueTypeEnum.MAX))){
                mwHistoryDTO.setValue(String.valueOf(mwHistoryDTO.getValueMax()));
                mwHistoryDTO.setLastValue(mwHistoryDTO.getValueMax());
            }
            if(valueType.equals(String.valueOf(MwValueTypeEnum.MIN))){
                mwHistoryDTO.setValue(String.valueOf(mwHistoryDTO.getValueMin()));
                mwHistoryDTO.setLastValue(mwHistoryDTO.getValueMin());
            }
        }
    }

    @Override
    public ApplicationTableInfos getApplicationTableInfos(QueryApplicationTableParam param) {
        ApplicationTableInfos tableInfos = new ApplicationTableInfos();
        Map<String, List<Map<String, Object>>> map = new HashMap<>();
        Map<String, List<ColTable>> titleMap = new HashMap<>();

        if (param.getApplicationNames() != null && param.getApplicationNames().size() > 0) {


            int coreSizePool = Runtime.getRuntime().availableProcessors() * 2 + 1;
            coreSizePool = (coreSizePool < param.getApplicationNames().size()) ? coreSizePool : param.getApplicationNames().size();//当使用cpu算出的线程数小于分页或未分页的数据条数时，使用cpu，否者使用数据条数
            ThreadPoolExecutor executorService = new ThreadPoolExecutor(coreSizePool, param.getApplicationNames().size() + 2, 60, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
            Map<String, Object> endMap = new HashMap<>();
            List<Future<Map<String, Object>>> futureList = new ArrayList<>();
            for (String applicationName : param.getApplicationNames()) {
                Callable callable = new Callable() {
                    @Override
                    public Map<String, Object> call() throws Exception {
                        Map<String, Object> map = getApplicationInfo(param.getMonitorServerId(), param.getAssetsId(), applicationName, param.getTypeName(), param.isHardwareFlag());
                        return map;
                    }
                };
                Future<Map<String, Object>> f = executorService.submit(callable);
                futureList.add(f);
            }
            for (Future<Map<String, Object>> f : futureList) {
                try {
                    Map<String, Object> result = f.get(10, TimeUnit.SECONDS);
                    endMap.putAll(result);
                } catch (Exception e) {
                    f.cancel(true);
                }
            }
            executorService.shutdown();
            log.info("关闭线程池");

//                List<Map<String, Object>> list = new ArrayList<>();
//                List<ColTable> colTables = new ArrayList<>();
//                long start = System.currentTimeMillis();
//                MWZabbixAPIResult dataByAppName = mwtpServerAPI.getItemDataByAppName(param.getMonitorServerId(), param.getAssetsId(), applicationName, param.getTypeName());
//                long end1 = System.currentTimeMillis();
//                log.info("测试api调用时间：time:{}", end1 - start);
//
//                if (!dataByAppName.isFail()) {
//                    boolean specialFlag = false;
//                    JsonNode jsonNode = (JsonNode) dataByAppName.getData();
//                    if (jsonNode != null && jsonNode.size() > 0) {
//                        List<ItemGetDTO> itemGetDTOS = JSONObject.parseArray(dataByAppName.getData().toString(), ItemGetDTO.class);
//                        List<String> valuemapIds = new ArrayList<>();
//                        for (ItemGetDTO item : itemGetDTOS) {
//                            String valuemapid = item.getValuemapid();
//                            valuemapIds.add(valuemapid);
//                        }
//                        List<String> valuemapIdList = valuemapIds.stream().distinct().collect(Collectors.toList());
//                        Map<String, Map> valueMapByIdMap = mwServerManager.getValueMapByIdList(param.getMonitorServerId(), valuemapIdList);
//                        itemGetDTOS.forEach(li -> {
//                            if (valueMapByIdMap != null && valueMapByIdMap.size() > 0 &&
//                                    valueMapByIdMap.get(li.getValuemapid()) != null && valueMapByIdMap.get(li.getValuemapid()).get(li.getLastvalue()) != null) {
//                                String newvalue = valueMapByIdMap.get(li.getValuemapid()).get(li.getLastvalue()).toString();
//                                if (!Strings.isNullOrEmpty(newvalue)) {
//                                    li.setLastvalue(newvalue);
//                                }
//                            }
//                        });
//                        if (itemGetDTOS.size() > 0) {
//                            String originalType = itemGetDTOS.get(0).getOriginalType();
//
//                            if (originalType != null && StringUtils.isNotEmpty(originalType)) {
//                                Map<String, List<ItemGetDTO>> collect = itemGetDTOS.stream().collect(Collectors.groupingBy(ItemGetDTO::getOriginalType));
//                                ItemGetDTO itemGetDTO = null;
//                                for (List<ItemGetDTO> value : collect.values()) {
//                                    Map<String, Object> lastMap = value.stream().collect(Collectors.toMap(ItemGetDTO::getName, ItemGetDTO::getLastvalue, (oldValue,newValue)->newValue));
//                                    Map<String, Object> sortLastMap = value.stream().collect(HashMap::new, (m, v) -> {
//                                        if (v.getSortName() != null && v.getSortLastValue() != null && StringUtils.isNotEmpty(v.getSortName())) {
//                                            m.put(v.getSortName(), v.getSortLastValue());
//                                        }
//                                    }, HashMap::putAll);
////                                    Map<String, Object> sortLastMap = value.stream().collect(Collectors.toMap(ItemGetDTO::getSortName, ItemGetDTO::getSortLastValue));
////                                    sortLastMap.entrySet().removeIf(entry -> entry.getValue() == null);//清空所有空值
//                                    itemGetDTO = value.get(0);
//                                    String firstType = itemGetDTO.getFirstType();
//                                    String secondType = itemGetDTO.getSecondType();
//                                    String cuspType = itemGetDTO.getCuspType();
//
//                                    if (firstType != null) {
//                                        lastMap.put("firstType", param.isHardwareFlag() ? originalType.substring(1, originalType.length() - 1) : firstType);
//                                    }
//                                    if (secondType != null && !param.isHardwareFlag()) {
//                                        lastMap.put("secondType", secondType);
//                                    }
//                                    if (cuspType != null && !param.isHardwareFlag()) {
//                                        lastMap.put("cuspType", cuspType);
//                                    }
//                                    lastMap.putAll(sortLastMap);
//                                    list.add(lastMap);
//                                }
//
//                            } else {//说明是普通的table，只需要监控项的中文名和value值组成的table
//                                for (int i = 0; i < itemGetDTOS.size(); i++) {
//                                    if (i == 0) {//遍历第一个需要new 一个map再赋值
//                                        Map<String, Object> newMap = new HashMap<>();
//                                        specialFlag = true;
//                                        newMap.put(mwServerManager.getChNameBase(itemGetDTOS.get(i).getName()), itemGetDTOS.get(i).getLastvalue());
//                                        list.add(newMap);
//                                    } else {
//                                        list.get(0).put(mwServerManager.getChNameBase(itemGetDTOS.get(i).getName()), itemGetDTOS.get(i).getLastvalue());
//                                    }
//                                }
//                            }
//                            log.info("测试");
//                        }
//                        if (specialFlag) {
//                            colTables.add(new ColTable("名称", null, true, true));
//                            colTables.add(new ColTable("值", null, false, true));
//                        } else {
//                            if (list != null && list.size() > 0) {
//                                for (String key : list.get(0).keySet()) {
//                                    if ("firstType".equals(key)) {
//                                        colTables.add(0, new ColTable("名称", key, true, true));
//                                    } else if ("secondType".equals(key)) {
//                                        colTables.add(0, new ColTable("SSIDS名称", key, true, true));
//                                    } else if ("cuspType".equals(key)) {
//                                        colTables.add(0, new ColTable("IP地址", key, true, true));
//                                    } else {
//                                        colTables.add(new ColTable(mwServerManager.getChNameBase(key), key, true, key.contains("sort") ? false : true));
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//                long end2 = System.currentTimeMillis();
//                log.info("测试data处理时间：time:{}", end2 - end1);
//                map.put(applicationName, list);
//
//                titleMap.put(applicationName, colTables);
//        }
            for (String applicationName : param.getApplicationNames()) {
                map.put(applicationName, (List<Map<String, Object>>) endMap.get("data_" + applicationName));
                titleMap.put(applicationName, (List<ColTable>) endMap.get("title_" + applicationName));
            }
            tableInfos.setAllData(map);
            tableInfos.setTitleData(titleMap);
        }
        return tableInfos;
    }


    public Map<String, Object> getApplicationInfo(Integer monitorServerId, String assetsId, String applicationName, String typeName, boolean hardwareFlag) {
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        List<ColTable> colTables = new ArrayList<>();
        long start = System.currentTimeMillis();
        MWZabbixAPIResult dataByAppName = mwtpServerAPI.getItemDataByAppName(monitorServerId, assetsId, applicationName, typeName);
        long end1 = System.currentTimeMillis();
        log.info("测试api调用时间：time:{}", end1 - start);

        if (!dataByAppName.isFail()) {
            boolean specialFlag = false;
            JsonNode jsonNode = (JsonNode) dataByAppName.getData();
            if (jsonNode != null && jsonNode.size() > 0) {
                List<ItemGetDTO> itemGetDTOS = JSONObject.parseArray(dataByAppName.getData().toString(), ItemGetDTO.class);
                List<String> valuemapIds = new ArrayList<>();
                for (ItemGetDTO item : itemGetDTOS) {
                    String valuemapid = item.getValuemapid();
                    valuemapIds.add(valuemapid);
                }
                List<String> valuemapIdList = valuemapIds.stream().distinct().collect(Collectors.toList());
                Map<String, Map> valueMapByIdMap = mwServerManager.getValueMapByIdList(monitorServerId, valuemapIdList);
                itemGetDTOS.forEach(li -> {
                    if (valueMapByIdMap != null && valueMapByIdMap.size() > 0 &&
                            valueMapByIdMap.get(li.getValuemapid()) != null && valueMapByIdMap.get(li.getValuemapid()).get(li.getLastvalue()) != null) {
                        String newvalue = valueMapByIdMap.get(li.getValuemapid()).get(li.getLastvalue()).toString();
                        if (!Strings.isNullOrEmpty(newvalue)) {
                            li.setLastvalue(newvalue);
                        }
                    }
                });
                if (itemGetDTOS.size() > 0) {
                    String originalType = itemGetDTOS.get(0).getOriginalType();

                    if (originalType != null && StringUtils.isNotEmpty(originalType)) {
                        Map<String, List<ItemGetDTO>> collect = itemGetDTOS.stream().collect(Collectors.groupingBy(ItemGetDTO::getOriginalType));
                        ItemGetDTO itemGetDTO = null;
                        for (List<ItemGetDTO> value : collect.values()) {
                            Map<String, Object> lastMap = value.stream().collect(Collectors.toMap(ItemGetDTO::getName, ItemGetDTO::getLastvalue, (oldValue, newValue) -> newValue));
                            Map<String, Object> sortLastMap = value.stream().collect(HashMap::new, (m, v) -> {
                                if (v.getSortName() != null && v.getSortLastValue() != null && StringUtils.isNotEmpty(v.getSortName())) {
                                    m.put(v.getSortName(), v.getSortLastValue());
                                }
                            }, HashMap::putAll);
//                                    Map<String, Object> sortLastMap = value.stream().collect(Collectors.toMap(ItemGetDTO::getSortName, ItemGetDTO::getSortLastValue));
//                                    sortLastMap.entrySet().removeIf(entry -> entry.getValue() == null);//清空所有空值
                            itemGetDTO = value.get(0);
                            String firstType = itemGetDTO.getFirstType();
                            String secondType = itemGetDTO.getSecondType();
                            String cuspType = itemGetDTO.getCuspType();
                            log.info("originalType:"+originalType+":applicationName:"+applicationName);
                            log.info("firstType:"+firstType+":applicationName:"+applicationName);
                            log.info("secondType:"+secondType+":applicationName:"+applicationName);
                            log.info("cuspType:"+cuspType+":applicationName:"+applicationName);
                            if (firstType != null) {
                                lastMap.put("firstType",firstType);
                            }
                            if (secondType != null && !hardwareFlag) {
                                lastMap.put("secondType", secondType);
                            }
                            if (cuspType != null && !hardwareFlag) {
                                lastMap.put("cuspType", cuspType);
                            }
                            lastMap.putAll(sortLastMap);
                            list.add(lastMap);
                        }

                    } else {//说明是普通的table，只需要监控项的中文名和value值组成的table
                        for (int i = 0; i < itemGetDTOS.size(); i++) {
                            if (i == 0) {//遍历第一个需要new 一个map再赋值
                                Map<String, Object> newMap = new HashMap<>();
                                specialFlag = true;
                                newMap.put(mwServerManager.getChNameBase(itemGetDTOS.get(i).getName()), itemGetDTOS.get(i).getLastvalue());
                                list.add(newMap);
                            } else {
                                list.get(0).put(mwServerManager.getChNameBase(itemGetDTOS.get(i).getName()), itemGetDTOS.get(i).getLastvalue());
                            }
                        }
                    }
                    log.info("测试");
                }
                if (specialFlag) {
                    colTables.add(new ColTable("名称", null, true, true));
                    colTables.add(new ColTable("值", null, false, true));
                } else {
                    if (list != null && list.size() > 0) {
                        for (String key : list.get(0).keySet()) {
                            if ("firstType".equals(key)) {
                                colTables.add(0, new ColTable("名称", key, true, true));
                            } else if ("secondType".equals(key)) {
                                colTables.add(0, new ColTable("SSIDS名称", key, true, true));
                            } else if ("cuspType".equals(key)) {
                                colTables.add(0, new ColTable("IP地址", key, true, true));
                            } else {
                                colTables.add(new ColTable(mwServerManager.getChNameBase(key), key, true, key.contains("sort") ? false : true));
                            }
                        }
                    }
                }
            }
        }
        long end2 = System.currentTimeMillis();
        log.info("测试data处理时间：time:{}", end2 - end1);
        map.put("data_" + applicationName, list);
        map.put("title_" + applicationName, colTables);
        return map;
    }
}
