package cn.mw.monitor.assets.service.impl;

import cn.mw.monitor.assets.dao.MwAssetsNoDataByItemDao;
import cn.mw.monitor.assets.service.MwAssetsNoDataByItemService;
import cn.mw.monitor.assets.utils.ExportExcel;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.DateUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author qzg
 * @date 2021/10/14
 */
@Service
@Slf4j
@Transactional
public class MwAssetsNoDataByItemImpl implements MwAssetsNoDataByItemService {
    private static final Logger logger = LoggerFactory.getLogger("MwAssetsNoDataByItemImpl");


    @Resource
    MwAssetsNoDataByItemDao mwAssetsNoDataByItemDao;
    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    @Override
    public Reply getNoDataAssets(HttpServletRequest request, HttpServletResponse response) {
        List<Map> mapList = mwAssetsNoDataByItemDao.getAssetsInfoAll();
        Map<Integer, List<String>> map = new HashMap();
        List<Map> listAllInfo = new ArrayList<>();
        Map<String, Map> mapInfo = new HashMap<>();
        if (mapList != null && mapList.size() > 0) {
            //对资产数据按照serverId进行分类，
            List<String> hostIds = new ArrayList<>();
            for (Map maps : mapList) {

                String assetsId = maps.get("assetsId").toString();
                Integer serverId = 0;
                if (maps.get("monitorServerId") != null) {
                    serverId = Integer.valueOf(maps.get("monitorServerId").toString());
                }
                if (map.containsKey(serverId)) {
                    hostIds = map.get(serverId);
                    hostIds.add(assetsId);
                    map.put(serverId, hostIds);
                } else {
                    map = new HashMap();
                    hostIds = new ArrayList<>();
                    hostIds.add(assetsId);
                    map.put(serverId, hostIds);
                }
                mapInfo.put(assetsId, maps);
            }
        }
        Map<String, String> itemMap = new HashMap<>();
        //所有的itemList
        List<String> itemIdListAll = new ArrayList<>();
        Map<String, Map> mapItemInfo = new HashMap<>();
        //最新数据没值的list集合
        List<String> itemIdListNoDataByLastValue = new ArrayList<>();
        //7天内有数据的list集合
        List<String> itemIdListDataBy7Day = new ArrayList<>();
        //有数据的list集合
        List<String> itemIdListData = new ArrayList<>();
        //有数据的list集合
        List<String> itemIdListNoData = new ArrayList<>();
        Date date = new Date();
        long endDate = date.getTime() / 1000;
        //开始时间，默认是7天前
        Long startDate = DateUtils.addDays(date, -10).getTime() / 1000;
        //对资产的monitorServerid循环。
        map.forEach((k, v) ->
        {
            Map<Integer, List<String>> itemByValueType = new HashMap<>();
            List<String> itemIdLists = new ArrayList<>();
            List<List<String>> subList = splitList(v, 100);

            int coreSizePool1 = Runtime.getRuntime().availableProcessors() * 2 + 1;
            coreSizePool1 = (coreSizePool1 > subList.size()) ? subList.size() : coreSizePool1;
            ThreadPoolExecutor executorService1 = new ThreadPoolExecutor(coreSizePool1, coreSizePool1 + 2, 10, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
            List<Future<MWZabbixAPIResult>> futureList1 = new ArrayList<>();
            List<MWZabbixAPIResult> listInfo1 = new ArrayList<>();

            for (List<String> list : subList) {
                Callable<MWZabbixAPIResult> callable = new Callable<MWZabbixAPIResult>() {
                    @Override
                    public MWZabbixAPIResult call() throws Exception {
                        //查询hostIdList 下所有的监控项
                        MWZabbixAPIResult resultDate = mwtpServerAPI.getItemDataByAllAssets(k, v);
                        return resultDate;
                    }
                };
                Future<MWZabbixAPIResult> submit = executorService1.submit(callable);
                futureList1.add(submit);
            }

            if (futureList1.size() > 0) {
                futureList1.forEach(f -> {
                    try {
                        MWZabbixAPIResult result = f.get(40, TimeUnit.SECONDS);
                        listInfo1.add(result);
                    } catch (Exception e) {
                        logger.error("fail to getNoDataAssets:对线程等待数据返回失败, case by {}", e);
                    }
                });
            }
            executorService1.shutdown();
            log.info("关闭线程池");


            for (MWZabbixAPIResult resultDate : listInfo1) {
                if (!resultDate.isFail()) {
                    JsonNode datas = (JsonNode) resultDate.getData();
                    if (datas.size() > 0) {
                        for (JsonNode itemsInfo : datas) {
                            String itemId = itemsInfo.get("itemid").asText();
                            itemIdListAll.add(itemId);
                            String itemName = itemsInfo.get("name").asText();
                            String lastvalue = itemsInfo.get("lastvalue").asText();
                            String hostid = itemsInfo.get("hostid").asText();
                            Integer valueType = itemsInfo.get("value_type").asInt();
                            Map m = new HashMap();
                            m.put("name", itemName);
                            m.put("hostId", hostid);
                            //避免itemId重复，加上monitorserverId
                            mapItemInfo.put(itemId + "_" + k, m);
                            if (Strings.isNullOrEmpty(lastvalue)) {
                                itemIdListNoDataByLastValue.add(itemId);
                                //根据返回值类型进行分类。进行历史数据查询时，需要用到数据类型
                                if (itemByValueType.containsKey(valueType)) {
                                    itemIdLists = itemByValueType.get(valueType);
                                    itemIdLists.add(itemId);
                                    itemByValueType.put(valueType, itemIdLists);
                                } else {
                                    itemIdLists = new ArrayList<>();
                                    itemIdLists.add(itemId);
                                    itemByValueType.put(valueType, itemIdLists);
                                }
                            }
                            itemMap.put(itemId, itemName);
                        }
                    }
                }
            }


            /////////////////////////////////////
            //将最新数据没有记录的监控，利用多线程，查询10天之内是否有数据。
            ////////////////////////////////////
            //对不同返回值类型进行循环，获取对应的itemIdList，key:返回值类型，val：对应的itemList
            final long startTime = startDate;
            final long endTime = endDate;
            itemByValueType.forEach((key, val) ->
            {
                List<List<String>> lists = splitList(val, 200);
                int coreSizePool2 = Runtime.getRuntime().availableProcessors() * 2 + 1;
                coreSizePool2 = (coreSizePool2 > lists.size()) ? lists.size() : coreSizePool2;
                ThreadPoolExecutor executorService2 = new ThreadPoolExecutor(coreSizePool2, coreSizePool2 + 2, 10, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
                List<Future<MWZabbixAPIResult>> futureList2 = new ArrayList<>();
                List<MWZabbixAPIResult> listInfo2 = new ArrayList<>();

                for (List<String> list : lists) {
                    Callable<MWZabbixAPIResult> callable = new Callable<MWZabbixAPIResult>() {
                        @Override
                        public MWZabbixAPIResult call() throws Exception {
                            MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.HistoryGetInfoByTimeAll(k, val, startTime, endTime, key);
                            return mwZabbixAPIResult;
                        }
                    };
                    Future<MWZabbixAPIResult> submit = executorService2.submit(callable);
                    futureList2.add(submit);
                }

                if (futureList2.size() > 0) {
                    futureList2.forEach(f -> {
                        try {
                            MWZabbixAPIResult result = f.get(40, TimeUnit.SECONDS);
                            listInfo2.add(result);
                        } catch (Exception e) {
                            logger.error("fail to getNoDataAssets:对线程等待数据返回失败, case by {}", e);
                        }
                    });
                }
                executorService2.shutdown();
                log.info("关闭线程池");
                for (MWZabbixAPIResult mwZabbixAPIResult : listInfo2) {
                    if (!mwZabbixAPIResult.isFail()) {
                        JsonNode dataList = (JsonNode) mwZabbixAPIResult.getData();
                        if (dataList.size() > 0) {
                            dataList.forEach(data -> {
                                //循环获取状态值。
                                String itemId = data.get("itemid").asText();
                                String value = data.get("value").asText();
                                //获取7天内数据的item，将所有没数据的去除7天内有数据的，得到7天内没有数据的itemList，
                                //再逐条查询历史记录，获取最新一条，没有值，则说明该item没有数据。
                                if (!Strings.isNullOrEmpty(value)) {
                                    itemIdListDataBy7Day.add(itemId);
                                }

                            });
                        }
                    }
                }
                //7天内历史数据有值的itemid 去重
                List<String> itemListHisByDistinctVal = itemIdListDataBy7Day.stream().distinct().collect(Collectors.toList());
                //将所有没值得 去掉7天有值的 ，剩下的无时间限制查询历史数据
                itemIdListNoDataByLastValue.removeAll(itemListHisByDistinctVal);

                int coreSizePool3 = Runtime.getRuntime().availableProcessors() * 2 + 1;
                coreSizePool3 = (coreSizePool3 > itemIdListNoDataByLastValue.size()) ? itemIdListNoDataByLastValue.size() : coreSizePool3;
                ThreadPoolExecutor executorService3 = new ThreadPoolExecutor(coreSizePool3, coreSizePool3 + 2, 10, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
                List<Future<MWZabbixAPIResult>> futureList3 = new ArrayList<>();
                List<MWZabbixAPIResult> listInfo3 = new ArrayList<>();
                for (String itemByVersion : itemIdListNoDataByLastValue) {
                    Callable<MWZabbixAPIResult> callable = new Callable<MWZabbixAPIResult>() {
                        @Override
                        public MWZabbixAPIResult call() throws Exception {
                            MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.HistoryGetByItemid(k, itemByVersion, key);
                            return mwZabbixAPIResult;
                        }
                    };
                    Future<MWZabbixAPIResult> submit = executorService3.submit(callable);
                    futureList3.add(submit);
                }
                if (futureList3.size() > 0) {
                    futureList3.forEach(f -> {
                        try {
                            MWZabbixAPIResult result = f.get(40, TimeUnit.SECONDS);
                            listInfo3.add(result);
                        } catch (Exception e) {
                            logger.error("fail to getNoDataAssets:对线程等待数据返回失败, case by {}", e);
                        }
                    });
                }
                executorService3.shutdown();
                for (MWZabbixAPIResult mwZabbixAPIResult : listInfo3) {
                    if (!mwZabbixAPIResult.isFail()) {
                        JsonNode dataList = (JsonNode) mwZabbixAPIResult.getData();
                        if (dataList.size() > 0) {
                            dataList.forEach(data -> {
                                //循环获取状态值。
                                String itemId = data.get("itemid").asText();
                                String value = data.get("value").asText();
                                if (Strings.isNullOrEmpty(value)) {
                                    itemIdListNoData.add(itemId);
                                }
                            });
                        }
                    }
                }

                for (String item : itemIdListNoData) {
                    Map m1 = new HashMap();
                    if (mapItemInfo != null && mapItemInfo.get(item+"_"+k) != null) {
                        Map map1 = mapItemInfo.get(item+"_"+k);
                        String itemName = map1.get("name").toString();
                        String hostId = map1.get("hostId").toString();
                        Map assetsInfo = mapInfo.get(hostId);
                        if (mapInfo != null && mapInfo.get(hostId) != null) {
                            m1.put("itemName", itemName);
                            m1.put("assetsName", assetsInfo.get("assetsName").toString());
                            m1.put("ip", assetsInfo.get("ip").toString());
                            m1.put("orgName", assetsInfo.get("orgName").toString());
                            m1.put("specifications", assetsInfo.get("specifications").toString());
                            listAllInfo.add(m1);
                        }
                    }
                }
            });
        });
        List<String> lable = Arrays.asList("assetsName", "ip", "specifications", "orgName", "itemName");
        List<String> lableName = Arrays.asList("资产名称", "IP地址", "规格型号", "机构", "监控项名称");
        try {
            ExportExcel.exportExcel("无数据资产监控项导出报表", "无数据资产监控项导出报表", lableName, lable, listAllInfo, "yyyy-MM-dd HH:mm:ss", response);
        } catch (IOException e) {
            logger.error("fail to getNoDataAssets:无数据资产监控项导出报表失败, case by {}", e);
        }
        return Reply.ok("无数据资产监控项导出报表成功！");
    }

    private List<List<String>> splitList(List<String> messagesList, int groupSize) {
        int length = messagesList.size();
        // 计算可以分成多少组
        int num = (length + groupSize - 1) / groupSize; // TODO
        List<List<String>> newList = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            // 开始位置
            int fromIndex = i * groupSize;
            // 结束位置
            int toIndex = (i + 1) * groupSize < length ? (i + 1) * groupSize : length;
            newList.add(messagesList.subList(fromIndex, toIndex));
        }
        return newList;
    }
}
