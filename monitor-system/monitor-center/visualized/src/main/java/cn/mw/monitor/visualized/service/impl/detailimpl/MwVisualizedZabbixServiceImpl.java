package cn.mw.monitor.visualized.service.impl.detailimpl;

import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.util.UnitsUtil;
import cn.mw.monitor.visualized.dao.MwVisualizedManageDao;
import cn.mw.monitor.visualized.dto.MwVisualizedIndexDto;
import cn.mw.monitor.visualized.dto.MwVisualizedZabbixDataDto;
import cn.mw.monitor.visualized.dto.MwVisualizedZabbixHistoryDto;
import cn.mw.monitor.visualized.param.MwVisualizedIndexQueryParam;
import cn.mw.monitor.visualized.service.MwVisualizedDataSourceService;
import cn.mw.monitor.visualized.service.impl.manager.MwVisualizedDataChangeManager;
import cn.mw.monitor.util.MwVisualizedDateUtil;
import cn.mw.monitor.visualized.util.MwVisualizedUnitChangeUtil;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @ClassName MwVisualizedZabbixServiceImpl
 * @Description 指标来源于zabbix数据查询
 * @Author gengjb
 * @Date 2022/4/26 11:05
 * @Version 1.0
 **/
@Service
@Slf4j
public class MwVisualizedZabbixServiceImpl implements MwVisualizedDataSourceService {

    @Autowired
    private MwVisualizedDataChangeManager dataChangeService;

    @Resource
    private MwVisualizedManageDao visualizedManageDao;


    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    @Override
    public int getDataSource() {
        return 1;
    }

    @Override
    public Object getData(MwVisualizedIndexQueryParam indexQueryParam) {
        try {
            //根据参数获取时间
            String startTime = indexQueryParam.getStartTime();
            String endTime = indexQueryParam.getEndTime();
            //存储查询历史记录的开始时间与结束时间
            List<Date> dates = new ArrayList<>();
            if(StringUtils.isBlank(startTime) && StringUtils.isBlank(endTime)){
                dates = MwVisualizedDateUtil.getDates(indexQueryParam.getType(), indexQueryParam.getDateType());
            }else{
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                dates.add(format.parse(startTime));
                dates.add(format.parse(endTime));
            }
            List<MwVisualizedIndexDto> indexDtos = indexQueryParam.getIndexDtos();
            Set<Integer> set = new HashSet<>();
            //获取指标中的item与类型信息
            Map<Integer,List<String>> itemMap = new HashMap<>();
            Map<String,MwVisualizedZabbixDataDto> zabbixDataDtos = new HashMap<>();
            Set<String> itemIdSets = new HashSet<>();
            if(CollectionUtils.isNotEmpty(indexDtos)){
                for (MwVisualizedIndexDto indexDto : indexDtos) {
                    String itemId = indexDto.getItemId();
                    Integer valueType = indexDto.getValueType();
                    Integer monitorServerId = indexDto.getMonitorServerId();
                    itemIdSets.add(itemId);
                    if(!itemMap.isEmpty() && itemMap.get(valueType) != null){
                        List<String> itemIds = itemMap.get(valueType);
                        itemIds.add(itemId);
                        itemMap.put(valueType,itemIds);
                    }else{
                        List<String> itemIds = new ArrayList<>();
                        itemIds.add(itemId);
                        itemMap.put(valueType,itemIds);
                    }
                    set.add(monitorServerId);
                    zabbixDataDtos.put(itemId,MwVisualizedZabbixDataDto.builder().hostId(indexDto.getAssetsId()).interfaceName(indexDto.getInterfaceName()).assetsName(indexDto.getAssetsName()).ip(indexDto.getIpAddress()).itemId(itemId).name(indexDto.getIndexMonitorItem()).units(indexDto.getOriginUnits()).originUtits(indexDto.getOriginUnits()).valueType(valueType).fieldName(indexDto.getIndexName()).isExport(indexQueryParam.getIsExport()).build());
                }
            }
            boolean flag = checkIsNotTrendValue(dates.get(0).getTime() / 1000, dates.get(1).getTime() / 1000);
            List<MwVisualizedZabbixDataDto> realData = new ArrayList<>();
            //判断是否查询趋势接口，大于一天查询趋势接口
            for (Integer serverId : set) {
                if(!flag){
                    for (Integer valueType : itemMap.keySet()) {
                        List<String> itemIds = itemMap.get(valueType);
                        MWZabbixAPIResult historyResult = mwtpServerAPI.HistoryGetInfoByTimeAll(serverId, itemIds, dates.get(0).getTime() / 1000, dates.get(1).getTime() / 1000, valueType);
                        realData.addAll(analysisZabbixHistoryData(zabbixDataDtos, historyResult));
                    }
                }else{
                    MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.trendBatchGet(serverId, Arrays.asList(itemIdSets.toArray(new String[0])), dates.get(0).getTime() / 1000, dates.get(1).getTime() / 1000);
                    realData.addAll(analysisZabbixTrendData(zabbixDataDtos, mwZabbixAPIResult));
                }
            }
            if(CollectionUtils.isEmpty(realData) && !zabbixDataDtos.isEmpty()){
                for (String key : zabbixDataDtos.keySet()) {
                    MwVisualizedZabbixDataDto zabbixDataDto = zabbixDataDtos.get(key);
                    realData.add(zabbixDataDto);
                }
            }
            return realData;
        }catch (Throwable e){
            log.error("查询组件zabbix数据失败,失败信息:",e);
            return e.getMessage();
        }
    }

    /**
     * 判断是否从趋势取值
     * @param startTime
     * @param endTime
     * @return
     */
    private boolean checkIsNotTrendValue(long startTime,long endTime){
        if((endTime-startTime) <= 86400){
            return false;
        }
        return true;
    }

    /**
     * 处理数据的资产信息
     * @param mwTangibleassetsTables
     * @param realData
     */
    private void handleAssetsNews(List<MwTangibleassetsTable> mwTangibleassetsTables,List<MwVisualizedZabbixDataDto> realData){
        if(CollectionUtils.isEmpty(realData))return;
        for (MwVisualizedZabbixDataDto realDatum : realData) {
            String hostId = realDatum.getHostId();
            for (MwTangibleassetsTable tangibleassetsTable : mwTangibleassetsTables) {
                String assetsId = tangibleassetsTable.getAssetsId();
                if(hostId.equals(assetsId)){
                    realDatum.setAssetsId(tangibleassetsTable.getId());
                    realDatum.setAssetsName(tangibleassetsTable.getAssetsName());
                    realDatum.setIp(tangibleassetsTable.getInBandIp());
                }
            }
        }
    }

    /**
     * 时间范围在最近12小时之内，查询历史数据接口
     * @param itemName 监控项名称
     * @param assetsMap 主机信息
     * @param dates 时间范围
     */
    private  List<MwVisualizedZabbixDataDto> getZabbixData(String itemName, Map<Integer, List<String>> assetsMap,List<Date> dates){
        List<MwVisualizedZabbixDataDto> realData = new ArrayList<>();
       //循环不同的服务器查询数据
        for (Integer serverId : assetsMap.keySet()) {
            //取值类型与itemid集合
            Map<Integer, List<String>> typeAndItemMap = new HashMap<>();
            //zabbix数据详情的集合
            Map<String,MwVisualizedZabbixDataDto> zabbixDataDtos = new HashMap<>();
            getZabbixItemNews(serverId,itemName,assetsMap.get(serverId),typeAndItemMap,zabbixDataDtos);
            if(typeAndItemMap.isEmpty() || CollectionUtils.isEmpty(zabbixDataDtos))continue;
            //根据itemId查询历史数据
            for (Integer type : typeAndItemMap.keySet()) {
                MWZabbixAPIResult historyResult = mwtpServerAPI.HistoryGetInfoByTimeAll(serverId, typeAndItemMap.get(type), dates.get(0).getTime() / 1000, dates.get(1).getTime() / 1000, type);
                //进行历史数据解析
                realData.addAll(analysisZabbixHistoryData(zabbixDataDtos,historyResult));
            }
        }
        return realData;
    }

    /**
     * 解析查询的历史记录数据
     * @param zabbixDataDtos
     * @param historyResult
     */
    private List<MwVisualizedZabbixDataDto> analysisZabbixHistoryData( Map<String,MwVisualizedZabbixDataDto> zabbixDataDtos,MWZabbixAPIResult historyResult){
        List<MwVisualizedZabbixDataDto> dataDtos = new ArrayList<>();
        if(historyResult == null || historyResult.getCode() != 0)return dataDtos;
        Map<String,List<MwVisualizedZabbixHistoryDto>> map = new HashMap<>();
        //数据集
        JsonNode jsonNode = (JsonNode) historyResult.getData();
        //循环数据集，拿到具体信息数据
        for (int i = 0; i < jsonNode.size(); i++){
            String itemid = jsonNode.get(i).get("itemid").asText();
            Double value = jsonNode.get(i).get("value").asDouble();
            long clock = jsonNode.get(i).get("clock").asLong();
            //将秒值转换为时间值
            Date date = new Date();
            date.setTime(clock*1000);
            if(!map.isEmpty() && map.containsKey(itemid)){
                List<MwVisualizedZabbixHistoryDto> mwVisualizedZabbixHistoryDtos = map.get(itemid);
                mwVisualizedZabbixHistoryDtos.add(MwVisualizedZabbixHistoryDto.builder().value(value).clock(date).build());
                map.put(itemid,mwVisualizedZabbixHistoryDtos);
            }else{
                List<MwVisualizedZabbixHistoryDto> mwVisualizedZabbixHistoryDtos = new ArrayList<>();
                mwVisualizedZabbixHistoryDtos.add(MwVisualizedZabbixHistoryDto.builder().value(value).clock(date).build());
                map.put(itemid,mwVisualizedZabbixHistoryDtos);
            }
        }
        //匹配item数据
        if(map.isEmpty() || zabbixDataDtos.isEmpty())return dataDtos;
        for (String itemid : map.keySet()) {
            List<MwVisualizedZabbixHistoryDto> historyDtos = map.get(itemid);
            MwVisualizedZabbixDataDto zabbixDataDto = zabbixDataDtos.get(itemid);
            if(CollectionUtils.isEmpty(historyDtos))continue;
            //数据存储在资产信息的DTO中
            zabbixDataDto.setValues(historyDtos);
            //计算出该数据的最大、最小、平均值
            Double max = new BigDecimal(String.valueOf(historyDtos.get(0).getValue())).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
            Double min = new BigDecimal(String.valueOf(historyDtos.get(0).getValue())).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
            Double sum = new Double(0);
            for (MwVisualizedZabbixHistoryDto historyDto : historyDtos) {
                Double value = historyDto.getValue();
                sum += value;
                if(max < value){
                    max = value;
                }
                if(min > value){
                    min = value;
                }
            }
            //设置最大、最小、平均值
            //转换单位
            Map<String, String> unitMap = UnitsUtil.getConvertedValue(new BigDecimal(max.toString()),zabbixDataDto.getUnits());
            if(unitMap == null){
                zabbixDataDto.setMaxValue(new BigDecimal(max).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                zabbixDataDto.setMinValue(new BigDecimal(min).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                zabbixDataDto.setAvgValue(new BigDecimal(sum/historyDtos.size()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            }else{
                zabbixDataDto.setMaxValue(new BigDecimal(unitMap.get("value")).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                unitMap = UnitsUtil.getConvertedValue(new BigDecimal(min.toString()),zabbixDataDto.getUnits());
                zabbixDataDto.setMinValue(new BigDecimal(unitMap.get("value")).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                unitMap = UnitsUtil.getConvertedValue(new BigDecimal(String.valueOf((sum/historyDtos.size()))),zabbixDataDto.getUnits());
                zabbixDataDto.setAvgValue(new BigDecimal(unitMap.get("value")).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                zabbixDataDto.setUnits(unitMap.get("units"));
            }
            dataDtos.add(zabbixDataDto);
        }
        return dataDtos;
    }

    /**
     * 查询zabbix上的Item信息
     * @param serverId
     * @param itemName
     * @param hostIds
     */
    private void getZabbixItemNews(Integer serverId,String itemName,List<String> hostIds,Map<Integer, List<String>> typeAndItemMap,Map<String,MwVisualizedZabbixDataDto> zabbixDataDtos){
        MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.itemGetbyFilter(serverId, itemName, hostIds);
        if(mwZabbixAPIResult == null || mwZabbixAPIResult.getCode() != 0)return;
        //数据集
        JsonNode jsonNode = (JsonNode) mwZabbixAPIResult.getData();
        //循环数据集，拿到具体信息数据
        for (int i = 0; i < jsonNode.size(); i++){
            String itemid = jsonNode.get(i).get("itemid").asText();
            String hostid = jsonNode.get(i).get("hostid").asText();
            String name = jsonNode.get(i).get("name").asText();
            String units = jsonNode.get(i).get("units").asText();
            Integer value_type = jsonNode.get(i).get("value_type").asInt();
            MwVisualizedZabbixDataDto zabbixDataDto = MwVisualizedZabbixDataDto.builder().hostId(hostid).itemId(itemid).name(name).units(units).valueType(value_type).build();
            zabbixDataDtos.put(itemid,zabbixDataDto);
            //见数据按照取值类型进行分组
            if(!typeAndItemMap.isEmpty() && typeAndItemMap.containsKey(value_type)){
                List<String> itemIds = typeAndItemMap.get(value_type);
                itemIds.add(itemid);
                typeAndItemMap.put(value_type,itemIds);
            }else{
                List<String> itemIds = new ArrayList<>();
                itemIds.add(itemid);
                typeAndItemMap.put(value_type,itemIds);
            }
        }
    }

    /**
     * 获取资产服务器分组情况
     * @param mwTangibleassetsTables 资产数据
     * @return
     */
    private Map<Integer,List<String>> getAssetsServiceNews(List<MwTangibleassetsTable> mwTangibleassetsTables){
        Map<Integer,List<String>> assetsMap = new HashMap<>();
        if(CollectionUtils.isEmpty(mwTangibleassetsTables))return assetsMap;
        //循环数据，将同一个服务ID的资产进行分组
        for (MwTangibleassetsTable mwTangibleassetsTable : mwTangibleassetsTables) {
            Integer monitorServerId = mwTangibleassetsTable.getMonitorServerId();
            String assetsId = mwTangibleassetsTable.getAssetsId();
            if(monitorServerId == null || StringUtils.isBlank(assetsId))continue;
            if(!assetsMap.isEmpty() && assetsMap.containsKey(monitorServerId)){
                List<String> assetsIds = assetsMap.get(monitorServerId);
                assetsIds.add(assetsId);
                assetsMap.put(monitorServerId,assetsIds);
            }else{
                List<String> assetsIds = new ArrayList<>();
                assetsIds.add(assetsId);
                assetsMap.put(monitorServerId,assetsIds);
            }
        }
        return assetsMap;
    }

    /**
     * 获取所有监控项
     * @param indexQueryParam
     * @return
     */
    private List<String> getAllItemName(List<MwVisualizedIndexQueryParam> indexQueryParam){
        List<String> itemName = new ArrayList<>();
        for (MwVisualizedIndexQueryParam mwVisualizedIndexQueryParam : indexQueryParam) {
            itemName.add(mwVisualizedIndexQueryParam.getIndexNameEng());
        }
        return itemName;
    }


    /**
     * 解析查询的趋势记录数据
     * @param zabbixDataDtos
     * @param historyResult
     */
    private List<MwVisualizedZabbixDataDto> analysisZabbixTrendData( Map<String,MwVisualizedZabbixDataDto> zabbixDataDtos,MWZabbixAPIResult historyResult){
        List<MwVisualizedZabbixDataDto> dataDtos = new ArrayList<>();
        if(historyResult == null || historyResult.getCode() != 0)return dataDtos;
        Map<String,List<MwVisualizedZabbixHistoryDto>> map = new HashMap<>();
        //数据集
        JsonNode jsonNode = (JsonNode) historyResult.getData();
        //循环数据集，拿到具体信息数据
        for (int i = 0; i < jsonNode.size(); i++){
            String itemid = jsonNode.get(i).get("itemid").asText();
            Double value_avg = jsonNode.get(i).get("value_avg").asDouble();
            long clock = jsonNode.get(i).get("clock").asLong();
            //将秒值转换为时间值
            Date date = new Date();
            date.setTime(clock*1000);
            if(!map.isEmpty() && map.containsKey(itemid)){
                List<MwVisualizedZabbixHistoryDto> mwVisualizedZabbixHistoryDtos = map.get(itemid);
                mwVisualizedZabbixHistoryDtos.add(MwVisualizedZabbixHistoryDto.builder().value(value_avg).clock(date).build());
                map.put(itemid,mwVisualizedZabbixHistoryDtos);
            }else{
                List<MwVisualizedZabbixHistoryDto> mwVisualizedZabbixHistoryDtos = new ArrayList<>();
                mwVisualizedZabbixHistoryDtos.add(MwVisualizedZabbixHistoryDto.builder().value(value_avg).clock(date).build());
                map.put(itemid,mwVisualizedZabbixHistoryDtos);
            }
        }
        //匹配item数据
        if(map.isEmpty() || zabbixDataDtos.isEmpty())return dataDtos;
        for (String itemid : map.keySet()) {
            List<MwVisualizedZabbixHistoryDto> historyDtos = map.get(itemid);
            MwVisualizedZabbixDataDto zabbixDataDto = zabbixDataDtos.get(itemid);
            if(CollectionUtils.isEmpty(historyDtos))continue;
            //数据存储在资产信息的DTO中
            zabbixDataDto.setValues(historyDtos);
            //计算出该数据的最大、最小、平均值
            Double max = historyDtos.get(0).getValue();
            Double min = historyDtos.get(0).getValue();
            Double sum = new Double(0);
            for (MwVisualizedZabbixHistoryDto historyDto : historyDtos) {
                Double value = historyDto.getValue();
                sum += value;
                if(max < value){
                    max = value;
                }
                if(min > value){
                    min = value;
                }
            }
            //设置最大、最小、平均值
            //转换单位
            Map<String, String> unitMap = MwVisualizedUnitChangeUtil.changeUnit(zabbixDataDto.getUnits(), max.toString());
            if(unitMap == null){
                zabbixDataDto.setMaxValue(new BigDecimal(max).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                zabbixDataDto.setMinValue(new BigDecimal(min).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                zabbixDataDto.setAvgValue(new BigDecimal(sum/historyDtos.size()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            }else{
                zabbixDataDto.setMaxValue(new BigDecimal(unitMap.get("value")).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                unitMap = MwVisualizedUnitChangeUtil.changeUnit(zabbixDataDto.getUnits(), min.toString());
                zabbixDataDto.setMinValue(new BigDecimal(unitMap.get("value")).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                unitMap = MwVisualizedUnitChangeUtil.changeUnit(zabbixDataDto.getUnits(), (sum/historyDtos.size())+"");
                zabbixDataDto.setAvgValue(new BigDecimal(unitMap.get("value")).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                zabbixDataDto.setUnits(unitMap.get("units"));
            }
            dataDtos.add(zabbixDataDto);
        }
        return dataDtos;
    }
}
