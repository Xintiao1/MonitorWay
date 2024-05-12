package cn.mw.monitor.wireless.service.impl;

import cn.mw.monitor.common.util.PageList;
import cn.mw.monitor.util.ListSortUtil;
import cn.mw.monitor.util.UnitsUtil;
import cn.mw.monitor.wireless.dto.QueryWirelessClientParam;
import cn.mw.monitor.wireless.dto.WirelessClientDTO;
import cn.mw.monitor.wireless.service.MwWirelessClientService;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author qzg
 * @date 2021/6/16
 */
@Service
@Slf4j
public class MwWirelessClientImpl implements MwWirelessClientService {
    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    @Override
    public Reply getClientInfo(QueryWirelessClientParam param) {
        Map<String, Map<String, String>> map = new HashMap();
        Map<String, String> ApMap = new HashMap();
        PageInfo pageInfo = new PageInfo<List>();
        PageList pageList = new PageList();
        long startTime = System.currentTimeMillis();
        //获取客户端应用集数据
        String[] strName = {"MW_CLIENTS_MACADDR", "MW_CLIENTS_TXBYTES", "MW_CLIENTS_RXBYTES", "MW_CLIENTS_CHANNELS", "MW_CLIENTS_RSSI"};
        MWZabbixAPIResult dataByClientInfo = new MWZabbixAPIResult();
//        List<MWZabbixAPIResult> listAll = new ArrayList<>();
        final LinkedBlockingQueue<MWZabbixAPIResult> listAll = new LinkedBlockingQueue();
        ThreadPoolExecutor executorService = new ThreadPoolExecutor(5, 6, 10, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
        List<Future<MWZabbixAPIResult>> futureList = new ArrayList<>();

        //数据量过多，zabbix接口获取不到数据，分开循环查询
        for (int x = 0; x < strName.length; x++) {
            final int x1 = x;
            GetWireLessClienThread getDiskListThread = new GetWireLessClienThread() {
                @Override
                public MWZabbixAPIResult call() throws Exception {
                    return getClientInfoData(param, "MW_WLANCLIENTS", strName[x1]);
                }
            };
            Future<MWZabbixAPIResult> f = executorService.submit(getDiskListThread);
            futureList.add(f);
        }
        futureList.forEach(f -> {
            try {
                MWZabbixAPIResult netListDto = f.get(20, TimeUnit.SECONDS);
                listAll.add(netListDto);
            } catch (Exception e) {
                f.cancel(true);
            }
        });
        executorService.shutdown();
        long endTime11 = System.currentTimeMillis();
        log.info("关闭线程池");
        //获取Ap端应用集名称
        MWZabbixAPIResult dataByApName = mwtpServerAPI.getItemDataByAppName(param.getMonitorServerId(), param.getAssetId(), "MW_ACCESSPOINTS", "MWAP_Name");
        long endTime1 = System.currentTimeMillis();
        if (!dataByApName.isFail()) {
            JsonNode dataList = (JsonNode) dataByApName.getData();
            log.info("获取Ap端应用集名称数据成功！");
            if (dataList.size() > 0) {
                dataList.forEach(datas -> {
                    String name = datas.get("name").asText();
                    String lastvalue = datas.get("lastvalue").asText();
                    ApMap.put(name, lastvalue);
                });
            }
        }
        for (MWZabbixAPIResult results : listAll) {
            if (!results.isFail() && results.getData() != null) {
                JsonNode dataList = (JsonNode) results.getData();
                log.info("获取客户端应用集数据成功！数据量为：" + dataList.size());
                if (dataList.size() > 0) {
                    dataList.forEach(data -> {
                        String name = data.get("name").asText();
                        String lastvalue = data.get("lastvalue").asText();
                        String unitsValue = "";
                        String units = data.get("units").asText();
                        if (!Strings.isNullOrEmpty(units)) {//如果有单位，进行数值转换
                            BigDecimal big = BigDecimal.valueOf(Double.valueOf(lastvalue));
                            Map<String, String> convertedValue = UnitsUtil.getConvertedValue(big, units);
                            unitsValue = convertedValue.get("value") + convertedValue.get("units");
                        } else {
                            unitsValue = lastvalue;
                        }
                        int i = name.lastIndexOf(">");
                        String fireName = name.substring(0, i + 1);
                        String lastName = name.substring(i + 1);
                        String lowerLastName = lastName.substring(0, 1).toLowerCase() + lastName.substring(1);
                        String ip = fireName.substring(fireName.indexOf("<") + 1, fireName.length() - 1);
                        String ssIdName = fireName.substring(fireName.indexOf(",") + 1, fireName.indexOf("]") - 1);
                        String typeName = fireName.substring(1, fireName.indexOf(","));
                        if (map.containsKey(fireName)) {//map已经添加，则只追加lastName字段和对应的值
                            Map m = map.get(fireName);
                            m.put(lowerLastName, unitsValue);
                            m.put("sort" + lastName, lastvalue);
                            map.put(fireName, m);
                        } else {//map中没有，则添家基础数据
                            Map p = new HashMap();
                            p.put(lowerLastName, unitsValue);
                            p.put("sort" + lastName, lastvalue);
                            p.put("clientsIp", ip);
                            p.put("clientsSSID", ssIdName);
                            String MWAP_Name = "[" + typeName + "]MWAP_Name";
                            //获取Ap应用集数据
                            p.put("accessPoint", ApMap.get(MWAP_Name));
                            map.put(fireName, p);
                        }
                    });
                }
            }
        }
        List<WirelessClientDTO> listDTO = new ArrayList<>();
        List<WirelessClientDTO> listSort = new ArrayList<>();
        long endTime2 = System.currentTimeMillis();
        //对数据进行过滤查询
        map.forEach((k, v) ->
        {
            if (v != null && v.size() != 0) {
                if ((!Strings.isNullOrEmpty(param.getQueryName())) && (!Strings.isNullOrEmpty(param.getQueryValue()))) {
                    if (v.get(param.getQueryName()) != null && v.get(param.getQueryName()).trim().equals(param.getQueryValue().trim())) {
                        log.info("数据过滤，过滤字段：" + param.getQueryName() + "；过滤值：" + param.getQueryValue());
                        WirelessClientDTO wirelessDeviceDTO = new WirelessClientDTO();
                        wirelessDeviceDTO = JSON.parseObject(JSON.toJSONString(v), WirelessClientDTO.class);
                        listDTO.add(wirelessDeviceDTO);
                    }
                } else {
                    WirelessClientDTO wirelessDeviceDTO = new WirelessClientDTO();
                    wirelessDeviceDTO = JSON.parseObject(JSON.toJSONString(v), WirelessClientDTO.class);
                    listDTO.add(wirelessDeviceDTO);
                }
            }
        });
        long endTime3 = System.currentTimeMillis();
        if (param.getSortField() != null && StringUtils.isNotEmpty(param.getSortField())) {//根据字段指定排序
            listSort = listDTO;
            ListSortUtil<WirelessClientDTO> finalsHotTableDtos = new ListSortUtil<>();
            String sort = "sort" + param.getSortField().substring(0, 1).toUpperCase() + param.getSortField().substring(1);
            //查看当前属性名称是否在对象中
            try {
                Field field = WirelessClientDTO.class.getDeclaredField(sort);
                finalsHotTableDtos.sort(listSort, sort, param.getSortMode());
            } catch (NoSuchFieldException e) {
                log.info("has no field", e);
                finalsHotTableDtos.sort(listSort, param.getSortField(), param.getSortMode());
            }
        } else {//默认AccessPoint排序
            listSort = listDTO.stream().sorted(Comparator.comparing(WirelessClientDTO::getClientsIp).reversed()).collect(Collectors.toList());
        }
        pageInfo.setTotal(listSort.size());
        long endTime4 = System.currentTimeMillis();
        List listByPage = pageList.getList(listSort, param.getPageNumber(), param.getPageSize());
        pageInfo.setList(listByPage);
        log.info("使用线程访问Zabbix服务耗时：" + (endTime11 - startTime) + "ms" +
                "数据处理耗时：" + (endTime2 - endTime1) + "ms" +
                "数据过滤耗时：" + (endTime3 - endTime2) + "ms" +
                "数据排序耗时：" + (endTime4 - endTime3) + "ms" +
                "数据大小为：" + listSort.size() +
                "总耗时：" + (endTime4 - startTime) + "ms");
        return Reply.ok(pageInfo);
    }

    private MWZabbixAPIResult getClientInfoData(QueryWirelessClientParam param, String applicationName, String itemName) {
        MWZabbixAPIResult dataByClientInfo = mwtpServerAPI.getItemDataByAppName(param.getMonitorServerId(), param.getAssetId(), applicationName, itemName);
        return dataByClientInfo;
    }
}
