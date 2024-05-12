package cn.mw.monitor.wireless.service.impl;

import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.util.ListSortUtil;
import cn.mw.monitor.util.SeverityUtils;
import cn.mw.monitor.util.UnitsUtil;
import cn.mw.monitor.wireless.dao.MwWirelessDataShowDao;
import cn.mw.monitor.wireless.dto.*;
import cn.mw.monitor.wireless.service.MwWirelessDataShowService;
import cn.mw.monitor.wireless.service.ReportUtil;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.DateUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.JsonNode;
import joptsimple.internal.Strings;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author qzg
 * @date 2021/6/23
 */
@Service
@Slf4j
public class MwWirelessDataShowImpl implements MwWirelessDataShowService {
    @Autowired
    private MWTPServerAPI mwtpServerAPI;
    @Resource
    private MwWirelessDataShowDao mwWirelessDataShowDao;

    @Override
    public Reply getUserNumByTime(QueryWirelessDataShowParam param) {
        //无线设备 assetsType = 2，assetsTypeSub=19
        List<MwTangibleassetsTable> assetsList = mwWirelessDataShowDao.selectAssetsByAssetsTypeId(param.getAssetsTypeId(), param.getAssetsTypeSubId());
        //获取所有无线设备的hostId
        List<String> strHostList = new ArrayList<>();
        //主机信息map
        Map<String, String> hostMap = new HashMap<>();
        List<Map> list = new ArrayList<>();
        Date date = new Date();
        long endTime = date.getTime() / 1000;
        //开始时间，默认是前一天
        long startTime = DateUtils.addDays(date, -1).getTime() / 1000;
        if (param.getDateType() != null) {
            switch (param.getDateType()) {//1：hour 2:day 3:week 4:month
                case 1:
                    startTime = DateUtils.addHours(date, -1).getTime() / 1000;
                    break;
                case 2:
                    startTime = DateUtils.addDays(date, -1).getTime() / 1000;
                    break;
                case 3:
                    startTime = DateUtils.addWeeks(date, -1).getTime() / 1000;
                    break;
                case 4:
                    startTime = DateUtils.addMonths(date, -1).getTime() / 1000;
                    break;
                case 5:
                    startTime = DateUtils.parse(param.getStartDate(), "yyyy-MM-dd HH:mm").getTime() / 1000;
                    endTime = DateUtils.parse(param.getEndDate(), "yyyy-MM-dd HH:mm").getTime() / 1000;
                default:
                    break;
            }
        }
        if (assetsList.size() > 0) {
            for (MwTangibleassetsTable dto : assetsList) {
                strHostList.add(dto.getAssetsId());
                hostMap.put(dto.getAssetsId(), dto.getAssetsName());
            }
        }
        //根据hostId查询zabbix服务，获取对应的itemId
        MWZabbixAPIResult itemResult = mwtpServerAPI.getItemDataByAppName(param.getMonitorServerId(), strHostList, "STATUS", "控制器总用户数");
        if (itemResult.getCode() == 0) {
            JsonNode data = (JsonNode) itemResult.getData();
            long finalStartTime = startTime;
            long finalEndTime = endTime;
            data.forEach(itemName -> {
                String itemid = itemName.get("itemid").asText();
                String hostid = itemName.get("hostid").asText();
                MWZabbixAPIResult uResult = mwtpServerAPI.GetHistoryByTimeAndType(param.getMonitorServerId(), itemid, finalStartTime, finalEndTime, 3);
                List<WirelessHistoryValueByLongDto> uvalueData = ReportUtil.getValueLongData(uResult);
                Map map = new HashMap();
                map.put("name", hostMap.get(hostid));
                map.put("monitorItem", "总用户数");
                map.put("dataList", uvalueData);
                map.put("units", "人");
                list.add(map);
            });
        }
        return Reply.ok(list);
    }

    /**
     * 发送、接收流量数据
     *
     * @param param
     * @return
     */
    @Override
    public Reply getFlowByTime(QueryWirelessDataShowParam param) {
        //无线设备 assetsType = 2，assetsTypeSub=19
        List<MwTangibleassetsTable> assetsList = mwWirelessDataShowDao.selectAssetsByAssetsTypeId(param.getAssetsTypeId(), param.getAssetsTypeSubId());
        //获取所有无线设备的hostId
        List<String> strHostList = new ArrayList<>();
        //主机信息map
        Map<String, String> hostMap = new HashMap<>();
        List<Map> list = new ArrayList<>();
        Date date = new Date();
        long endTime = date.getTime() / 1000;
        //开始时间，默认是前一天
        long startTime = DateUtils.addDays(date, -1).getTime() / 1000;
        if (param.getDateType() != null) {
            switch (param.getDateType()) {//1：hour 2:day 3:week 4:month
                case 1:
                    startTime = DateUtils.addHours(date, -1).getTime() / 1000;
                    break;
                case 2:
                    startTime = DateUtils.addDays(date, -1).getTime() / 1000;
                    break;
                case 3:
                    startTime = DateUtils.addWeeks(date, -1).getTime() / 1000;
                    break;
                case 4:
                    startTime = DateUtils.addMonths(date, -1).getTime() / 1000;
                    break;
                case 5:
                    startTime = DateUtils.parse(param.getStartDate(), "yyyy-MM-dd HH:mm").getTime() / 1000;
                    endTime = DateUtils.parse(param.getEndDate(), "yyyy-MM-dd HH:mm").getTime() / 1000;
                    break;
                default:
                    break;
            }
        }
        if (assetsList.size() > 0) {
            for (MwTangibleassetsTable dto : assetsList) {
                strHostList.add(dto.getAssetsId());
                hostMap.put(dto.getAssetsId(), dto.getAssetsName());
            }
        }
        //根据hostId查询zabbix服务，获取对应的itemId
        //发送数据总流量
        MWZabbixAPIResult itemTXResult = mwtpServerAPI.getItemDataByAppName(param.getMonitorServerId(), strHostList, "STATUS", "发送总流量");
        //接收总流量
        MWZabbixAPIResult itemRXResult = mwtpServerAPI.getItemDataByAppName(param.getMonitorServerId(), strHostList, "STATUS", "接收总流量");
        //发送流量
        if (itemTXResult.getCode() == 0) {
            JsonNode data = (JsonNode) itemTXResult.getData();
            long finalStartTime = startTime;
            long finalEndTime = endTime;
            data.forEach(itemName -> {
                Map map = getFlowMap(itemName, param, finalStartTime, finalEndTime, hostMap);
                map.put("monitorItem", "发送流量");
                list.add(map);
            });
        }
        //接受流量
        if (itemRXResult.getCode() == 0) {
            JsonNode data = (JsonNode) itemRXResult.getData();
            long finalStartTime = startTime;
            long finalEndTime = endTime;
            data.forEach(itemName -> {
                Map map = getFlowMap(itemName, param, finalStartTime, finalEndTime, hostMap);
                map.put("monitorItem", "接收流量");
                list.add(map);
            });
        }
        return Reply.ok(list);
    }

    /**
     * 流量数据处理
     *
     * @param itemName
     * @param param
     * @param startTime
     * @param endTime
     * @param hostMap
     * @return
     */
    public Map getFlowMap(JsonNode itemName, QueryWirelessDataShowParam param, long startTime, long endTime, Map<String, String> hostMap) {
        String itemid = itemName.get("itemid").asText();
        String hostid = itemName.get("hostid").asText();
        MWZabbixAPIResult uResult = mwtpServerAPI.GetHistoryByTimeAndType(param.getMonitorServerId(), itemid, startTime, endTime, 3);
        List<WirelessHistoryValueByDoubleDto> uvalueData = ReportUtil.getValueDoubleDataAndUnits(uResult);
        String maxValue = String.valueOf(uvalueData.stream().mapToDouble(WirelessHistoryValueByDoubleDto::getValue).max().getAsDouble());
        BigDecimal big = BigDecimal.valueOf(Double.valueOf(maxValue));
        Map<String, String> convertedValue = UnitsUtil.getConvertedValue(big, "B");
        String units = convertedValue.get("units");
        Double value = 0.0;
        for (WirelessHistoryValueByDoubleDto dto : uvalueData) {
            if ("KB".equals(units)) {
                value = dto.getValue() / 1024;
            }
            if ("MB".equals(units)) {
                value = dto.getValue() / 1024 / 1024;
            }
            if ("GB".equals(units)) {
                value = dto.getValue() / 1024 / 1024 / 1024;
            }
            Double values = new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            dto.setValue(values);
        }
        Map map = new HashMap();
        map.put("name", hostMap.get(hostid));
        map.put("dataList", uvalueData);
        map.put("units", convertedValue.get("units"));
        return map;
    }


    /**
     * 获取发送流量数据
     *
     * @param param
     * @return
     */
    @Override
    public Reply getDataByTXBytes(QueryWirelessDataShowParam param) {
        List<MwTangibleassetsTable> assetsList = mwWirelessDataShowDao.selectAssetsByAssetsTypeId(param.getAssetsTypeId(), param.getAssetsTypeSubId());
        Map<String, String> hostMap = new HashMap<>();
        List<String> strHostList = new ArrayList<>();
        MWZabbixAPIResult dataByTxBytesSSIDInfo = new MWZabbixAPIResult();
        MWZabbixAPIResult dataByTxBytesClientInfo = new MWZabbixAPIResult();
        List<WirelessItemData> list;
        if (assetsList.size() > 0) {
            for (MwTangibleassetsTable dto : assetsList) {
                strHostList.add(dto.getAssetsId());
                hostMap.put(dto.getAssetsId(), dto.getAssetsName());
            }
        }
        //设备类型 1：SSID端， 2:Client 客户端
        if ("1".equals(param.getDeviceType())) {
            //SSID端发送数据流量
            dataByTxBytesSSIDInfo = mwtpServerAPI.getItemDataByAppName(param.getMonitorServerId(), strHostList, "MW_WLANSSIDS", "MWSSIDS_TxBytes");
            list = dataHanding(hostMap, dataByTxBytesSSIDInfo, "1");
        } else {
            //客户端发送数据流量
            dataByTxBytesClientInfo = mwtpServerAPI.getItemDataByAppName(param.getMonitorServerId(), strHostList, "MW_WLANCLIENTS", "MW_CLIENTS_TXBYTES");
            list = dataHanding(hostMap, dataByTxBytesClientInfo, "2");
        }
        List<WirelessItemData> listSort = list.stream().sorted(Comparator.comparing(WirelessItemData::getValue).reversed()).collect(Collectors.toList());
        int index = 5;
        if (param.getIndexTop() != null && param.getIndexTop() != 0) {
            index = param.getIndexTop();
        }
        List<WirelessItemData> subList;
        if (listSort.size() < index) {
            subList = listSort.subList(0, listSort.size());
        } else {
            subList = listSort.subList(0, index);
        }
        long sum = 0L;
        sum = subList.stream().mapToLong(WirelessItemData::getValue).sum();
        Map map = new HashMap();
        for (WirelessItemData data : subList) {
            data.setNum(sum);
        }
        return Reply.ok(subList);
    }

    /**
     * 获取接收流量数据
     *
     * @param param
     * @return
     */
    @Override
    public Reply getDataByRXBytes(QueryWirelessDataShowParam param) {
        List<MwTangibleassetsTable> assetsList = mwWirelessDataShowDao.selectAssetsByAssetsTypeId(param.getAssetsTypeId(), param.getAssetsTypeSubId());
        Map<String, String> hostMap = new HashMap<>();
        List<String> strHostList = new ArrayList<>();
        MWZabbixAPIResult dataByTxBytesSSIDInfo = new MWZabbixAPIResult();
        MWZabbixAPIResult dataByTxBytesClientInfo = new MWZabbixAPIResult();
        List<WirelessItemData> list;
        if (assetsList.size() > 0) {
            for (MwTangibleassetsTable dto : assetsList) {
                strHostList.add(dto.getAssetsId());
                hostMap.put(dto.getAssetsId(), dto.getAssetsName());
            }
        }
        //设备类型 1：SSID端， 2:Client 客户端
        if ("1".equals(param.getDeviceType())) {
            //SSID端接收数据流量
            dataByTxBytesSSIDInfo = mwtpServerAPI.getItemDataByAppName(param.getMonitorServerId(), strHostList, "MW_WLANSSIDS", "MWSSIDS_RxBytes");
            list = dataHanding(hostMap, dataByTxBytesSSIDInfo, "1");
        } else {
            //客户端接收数据流量
            dataByTxBytesClientInfo = mwtpServerAPI.getItemDataByAppName(param.getMonitorServerId(), strHostList, "MW_WLANCLIENTS", "MW_CLIENTS_RXBYTES");
            list = dataHanding(hostMap, dataByTxBytesClientInfo, "2");
        }
        List<WirelessItemData> listSort = list.stream().sorted(Comparator.comparing(WirelessItemData::getValue).reversed()).collect(Collectors.toList());
        int index = 5;
        if (param.getIndexTop() != null && param.getIndexTop() != 0) {
            index = param.getIndexTop();
        }
        List<WirelessItemData> subList;
        if (listSort.size() < index) {
            subList = listSort.subList(0, listSort.size());
        } else {
            subList = listSort.subList(0, index);
        }
        long sum = 0L;
        sum = subList.stream().mapToLong(WirelessItemData::getValue).sum();
        Map map = new HashMap();
        for (WirelessItemData data : subList) {
            data.setNum(sum);
        }
        return Reply.ok(subList);
    }

    /**
     * 获取信号强度数据(仅客户端)
     *
     * @param param
     * @return
     */
    @Override
    public Reply getDataByRSSI(QueryWirelessDataShowParam param) {
        List<MwTangibleassetsTable> assetsList = mwWirelessDataShowDao.selectAssetsByAssetsTypeId(param.getAssetsTypeId(), param.getAssetsTypeSubId());
        Map<String, String> hostMap = new HashMap<>();
        List<String> strHostList = new ArrayList<>();
        MWZabbixAPIResult dataByTxBytesClientInfo = new MWZabbixAPIResult();
        List<WirelessItemData> list;
        if (assetsList.size() > 0) {
            for (MwTangibleassetsTable dto : assetsList) {
                strHostList.add(dto.getAssetsId());
                hostMap.put(dto.getAssetsId(), dto.getAssetsName());
            }
        }
        //客户端信号强度数据
        dataByTxBytesClientInfo = mwtpServerAPI.getItemDataByAppName(param.getMonitorServerId(), strHostList, "MW_WLANCLIENTS", "MW_CLIENTS_RSSI");
        list = dataHanding(hostMap, dataByTxBytesClientInfo, "2");
        List<WirelessItemData> listSort = list.stream().sorted(Comparator.comparing(WirelessItemData::getValue).reversed()).collect(Collectors.toList());
        int index = 5;
        if (param.getIndexTop() != null && param.getIndexTop() != 0) {
            index = param.getIndexTop();
        }
        List<WirelessItemData> subList;
        if (listSort.size() < index) {
            subList = listSort.subList(0, listSort.size());
        } else {
            subList = listSort.subList(0, index);
        }
        long sum = 0L;
        sum = subList.stream().mapToLong(WirelessItemData::getValue).sum();
        Map map = new HashMap();
        for (WirelessItemData data : subList) {
            data.setNum(sum);
        }
        return Reply.ok(subList);
    }


    /**
     * 查询无线设备基本监控信息
     *
     * @param param
     * @return
     */
    @Override
    public Reply getWirelessDeviceInfo(QueryWirelessDataShowParam param) {
        //无线设备 assetsType = 2，assetsTypeSub=19
        List<MwTangibleassetsTable> assetsList = mwWirelessDataShowDao.selectAssetsByAssetsTypeId(param.getAssetsTypeId(), param.getAssetsTypeSubId());
        //获取所有无线设备的hostId
        List<String> strHostList = new ArrayList<>();
        Map<String, Map> map = new HashMap();
        Map<String, String> hostMap = new HashMap<>();
        if (assetsList.size() > 0) {
            for (MwTangibleassetsTable dto : assetsList) {
                strHostList.add(dto.getAssetsId());
                hostMap.put(dto.getAssetsId(), dto.getAssetsName());
                MWZabbixAPIResult APNumResult = mwtpServerAPI.getItemDataByAppName(param.getMonitorServerId(), strHostList, "MW_ACCESSPOINTS", "MWAP_Name");
                if (APNumResult.getCode() == 0) {
                    JsonNode data = (JsonNode) APNumResult.getData();
                    Map dataMap;
                    if (map.containsKey(dto.getAssetsId())) {
                        dataMap = map.get(dto.getAssetsId());
                    } else {
                        dataMap = new HashMap();
                    }
                    dataMap.put("apNum", data.size());
                    dataMap.put("sortApNum", data.size());
                    dataMap.put("updateData", new Date());
                    map.put(dto.getAssetsId(), dataMap);
                }
            }
        }
        //获取CPU利用率
        MWZabbixAPIResult CPUResult = mwtpServerAPI.getItemDataByAppName(param.getMonitorServerId(), strHostList, "性能", "CPU利用率");
        if (CPUResult.getCode() == 0) {
            JsonNode data = (JsonNode) CPUResult.getData();
            data.forEach(itemName -> {
                String hostid = itemName.get("hostid").asText();
                Map dataMap;
                if (map.containsKey(hostid)) {
                    dataMap = map.get(hostid);
                } else {
                    dataMap = new HashMap();
                }
                Double lastvalue = itemName.get("lastvalue").asDouble();
                dataMap.put("cpuNum", lastvalue);
                dataMap.put("sortCpuNum", lastvalue);
                map.put(hostid, dataMap);
            });
        }

        //获取多个host的 控制器MAC地址、控制器序列号、控制器规格型号、控制器软件版本
        MWZabbixAPIResult itemResult = mwtpServerAPI.getItemDataByAppName(param.getMonitorServerId(), strHostList, "硬件", "");
        if (itemResult.getCode() == 0) {
            JsonNode data = (JsonNode) itemResult.getData();
            data.forEach(itemName -> {
                String hostid = itemName.get("hostid").asText();
                String name = itemName.get("name").asText();
                String lastvalue = itemName.get("lastvalue").asText();
                Map dataMap;
                if (map.containsKey(hostid)) {
                    dataMap = map.get(hostid);
                } else {
                    dataMap = new HashMap();
                }
                if ("控制器MAC地址".equals(name)) {
                    dataMap.put("MAC", lastvalue);
                }
                if ("控制器序列号".equals(name)) {
                    dataMap.put("serialNo", lastvalue);
                }
                if ("控制器规格型号".equals(name)) {
                    dataMap.put("deviceModel", lastvalue);
                }
                if ("控制器软件版本".equals(name)) {
                    dataMap.put("versionInfo", lastvalue);
                }
                map.put(hostid, dataMap);
            });
        }
        //获取运行时间，用户数
        MWZabbixAPIResult durationAndUserNumResult = mwtpServerAPI.getItemDataByAppName(param.getMonitorServerId(), strHostList, "STATUS", "");
        if (durationAndUserNumResult.getCode() == 0) {
            JsonNode data = (JsonNode) durationAndUserNumResult.getData();
            data.forEach(itemName -> {
                String hostid = itemName.get("hostid").asText();
                String name = itemName.get("name").asText();
                String units = itemName.get("units").asText();
                Long lastvalue = itemName.get("lastvalue").asLong();
                Map dataMap;
                if (map.containsKey(hostid)) {
                    dataMap = map.get(hostid);
                } else {
                    dataMap = new HashMap();
                }
                if ("uptime".equals(units)) {
                    dataMap.put("duration", SeverityUtils.getLastTime(lastvalue));
                }
                if ("控制器总用户数".equals(name)) {
                    dataMap.put("userNum", lastvalue);
                    dataMap.put("sortUserNum", lastvalue);
                }
                map.put(hostid, dataMap);
            });
        }
        List<QueryWirelessDataShowDTO> listInfo = new ArrayList<>();
        if (assetsList.size() > 0) {
            for (MwTangibleassetsTable dto : assetsList) {
                Map mapInfo = map.get(dto.getAssetsId());
                QueryWirelessDataShowDTO wirelessDataShow = JSON.parseObject(JSON.toJSONString(mapInfo), QueryWirelessDataShowDTO.class);
                listInfo.add(wirelessDataShow);
            }
        }
        if (param.getSortField() != null && StringUtils.isNotEmpty(param.getSortField())) {//根据字段指定排
            ListSortUtil<QueryWirelessDataShowDTO> finalsHotTableDtos = new ListSortUtil<>();
            String sort = "sort" + param.getSortField().substring(0, 1).toUpperCase() + param.getSortField().substring(1);
            //查看当前属性名称是否在对象中
            try {
                Field field = QueryWirelessDataShowDTO.class.getDeclaredField(sort);
                finalsHotTableDtos.sort(listInfo, sort, param.getSortMode());
            } catch (NoSuchFieldException e) {
                log.info("has no field", e);
                finalsHotTableDtos.sort(listInfo, param.getSortField(), param.getSortMode());
            }
        }


        return Reply.ok(listInfo);
    }

    /**
     * SSID端信息
     *
     * @param param
     * @return
     */
    @Override
    public Reply getRSSIDeviceInfo(QueryWirelessDataShowParam param) {
        //无线设备 assetsType = 2，assetsTypeSub=19
        List<MwTangibleassetsTable> assetsList = mwWirelessDataShowDao.selectAssetsByAssetsTypeId(param.getAssetsTypeId(), param.getAssetsTypeSubId());
        //获取所有无线设备的hostId
        List<String> strHostList = new ArrayList<>();
        Map<String, Map> map = new HashMap();
        Map<String, String> hostMap = new HashMap<>();
        if (assetsList.size() > 0) {
            for (MwTangibleassetsTable dto : assetsList) {
                strHostList.add(dto.getAssetsId());
                hostMap.put(dto.getAssetsId(), dto.getAssetsName());
            }
        }
        MWZabbixAPIResult SSIDInfoResult = mwtpServerAPI.getItemDataByAppName(param.getMonitorServerId(), strHostList, "MW_WLANSSIDS", "");
        if (SSIDInfoResult.getCode() == 0) {
            JsonNode data = (JsonNode) SSIDInfoResult.getData();
            data.forEach(itemName -> {
                String name = itemName.get("name").asText();
                String names = name.substring(name.indexOf("[") + 1, name.indexOf("]"));
                String lastvalue = itemName.get("lastvalue").asText();
                String hostid = itemName.get("hostid").asText();
                Map maps;
                if (map.containsKey(names)) {
                    maps = map.get(names);
                } else {
                    maps = new HashMap();
                    maps.put("assetName", hostMap.get(hostid));
                }
                if (name.indexOf("MWSSIDS_Clients") != -1) {//用户数
                    maps.put("userNum", lastvalue);
                    maps.put("sortUserNum", lastvalue);
                }
                if (name.indexOf("MWSSIDS_Name") != -1) {//名称
                    maps.put("name", lastvalue);
                }
                if (name.indexOf("MWSSIDS_RxBytes") != -1) {//接收数据
                    BigDecimal big = BigDecimal.valueOf(Double.valueOf(lastvalue));
                    Map<String, String> convertedValue = UnitsUtil.getConvertedValue(big, "B");
                    String unitsValue = convertedValue.get("value") + convertedValue.get("units");
                    maps.put("rxBytesInfo", unitsValue);
                    maps.put("sortRxBytesInfo", lastvalue);
                }
                if (name.indexOf("MWSSIDS_TxBytes") != -1) {//发送数据
                    BigDecimal big = BigDecimal.valueOf(Double.valueOf(lastvalue));
                    Map<String, String> convertedValue = UnitsUtil.getConvertedValue(big, "B");
                    String unitsValue = convertedValue.get("value") + convertedValue.get("units");
                    maps.put("txBytesInfo", unitsValue);
                    maps.put("sortTxBytesInfo", lastvalue);
                }
                map.put(names, maps);
            });
        }
        List<QueryWirelessSSIDDataShowDTO> listDTO = new ArrayList<>();
        List<QueryWirelessSSIDDataShowDTO> listDTOs = new ArrayList<>();
        map.forEach((k, v) ->
        {
            if (v != null && v.size() != 0) {
                QueryWirelessSSIDDataShowDTO wirelessDeviceDTO = new QueryWirelessSSIDDataShowDTO();
                wirelessDeviceDTO = JSON.parseObject(JSON.toJSONString(v), QueryWirelessSSIDDataShowDTO.class);
                listDTO.add(wirelessDeviceDTO);
            }
        });
        if (param.getSortField() != null && StringUtils.isNotEmpty(param.getSortField())) {//根据字段指定排序
            ListSortUtil<QueryWirelessSSIDDataShowDTO> finalsHotTableDtos = new ListSortUtil<>();
            String sort = "sort" + param.getSortField().substring(0, 1).toUpperCase() + param.getSortField().substring(1);
            //查看当前属性名称是否在对象中
            try {
                Field field = QueryWirelessSSIDDataShowDTO.class.getDeclaredField(sort);
                listDTOs = finalsHotTableDtos.sort(listDTO, sort, param.getSortMode());
            } catch (NoSuchFieldException e) {
                log.info("has no field", e);
                listDTOs = finalsHotTableDtos.sort(listDTO, param.getSortField(), param.getSortMode());
            }
        } else {//默认AccessPoint排序
            listDTOs = listDTO.stream().sorted(Comparator.comparing(QueryWirelessSSIDDataShowDTO::getUserNum).reversed()).collect(Collectors.toList());
        }
        return Reply.ok(listDTOs);
    }


    /**
     * 数据处理
     *
     * @param hostMap
     * @param dataInfo
     * @param deviceType
     * @return
     */
    public List dataHanding(Map<String, String> hostMap, MWZabbixAPIResult dataInfo, String deviceType) {
        List list = new ArrayList();
        if (!dataInfo.isFail()) {
            JsonNode dataList = (JsonNode) dataInfo.getData();
            log.info("获取发送数据流量成功！");
            if (dataList.size() > 0) {
                dataList.forEach(datas -> {
                    String names = datas.get("name").asText();
                    String hostId = datas.get("hostid").asText();
                    String name;
                    if ("2".equals(deviceType) && names.indexOf("<") > -1) {//名称中带有<>，为客户端
                        name = "client_" + names.substring(names.indexOf("<") + 1, names.indexOf(">"));
                    } else {
                        name = "SSID_" + names.substring(names.indexOf("[") + 1, names.indexOf("]"));
                    }
                    Long lastvalue = datas.get("lastvalue").asLong();
                    String units = datas.get("units").asText();
                    String deviceName = hostMap.get(hostId) + "_" + name;//资产名+SSID名称。
                    WirelessItemData wirelessItemData = new WirelessItemData();
                    wirelessItemData.setName(deviceName);
                    wirelessItemData.setValue(lastvalue);
                    if (!Strings.isNullOrEmpty(units)) {
                        BigDecimal big = BigDecimal.valueOf(Double.valueOf(lastvalue));
                        Map<String, String> convertedValue = UnitsUtil.getConvertedValue(big, units.toUpperCase());
                        String unitsValue = convertedValue.get("value") + convertedValue.get("units");
                        wirelessItemData.setUnitsValue(unitsValue);
                    } else {
                        wirelessItemData.setUnitsValue(String.valueOf(lastvalue));
                    }
                    list.add(wirelessItemData);
                });
            }
        }
        return list;
    }

}
