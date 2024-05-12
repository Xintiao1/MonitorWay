package cn.mw.monitor.server.service.impl;

import cn.mw.monitor.common.constant.ZabbixItemConstant;
import cn.mw.monitor.server.dao.ItemNameDao;
import cn.mw.monitor.server.dao.TangibleOutbandDao;
import cn.mw.monitor.server.serverdto.*;
import cn.mw.monitor.server.serverdto.ApplicationDTO;
import cn.mw.monitor.server.service.ResultResolver;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.service.assets.model.RedisItemHistoryDto;
import cn.mw.monitor.service.server.api.MwServerCommons;
import cn.mw.monitor.service.server.api.dto.*;
import cn.mw.monitor.service.server.param.QueryAssetsAvailableParam;
import cn.mw.monitor.util.SeverityUtils;
import cn.mw.monitor.util.UnitsUtil;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mw.zbx.common.ZbxConstants;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author xhy
 * @date 2020/4/28 13:43
 */
@Component
@Slf4j
public class MwServerManager implements MwServerCommons, InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger("MwServerManager");
    private List<Pattern> ifPatternList;
    private Map<Pattern , HostIfPerformanceInfoCallback> ifCallbackMap;

    @Autowired
    private MWTPServerAPI mwtpServerAPI;
    @Resource
    private ItemNameDao itemNameDao;
    @Resource
    private TangibleOutbandDao tangibleOutbandDao;

    @Autowired
    private ResultResolver resultResolver;


    public ItemRank getItemRank(ServerDTO serverDto) {
        ItemRank itemRank = new ItemRank();
        List<ItemNameRank> list = new ArrayList<>();
        //根据主机和监控名称批量查询lastvalue search查询
        MWZabbixAPIResult result = mwtpServerAPI.itemGetbySearch(serverDto.getMonitorServerId(), serverDto.getName(), serverDto.getAssetsId());
        log.info("MwServerManager  {getItemRank}"+result);
        if(result != null && !result.isFail()){
            JsonNode itemData = (JsonNode) result.getData();
            if (itemData.size() > 0) {
                itemData.forEach(item -> {
                    ItemNameRank itemNameRank = new ItemNameRank();
                    String nameType = item.get("name").asText();
                    itemNameRank.setType(nameType.substring(1, nameType.indexOf("]")));
                    BigDecimal bigDecimal = new BigDecimal(item.get("lastvalue").asDouble());
                    double v = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    itemNameRank.setLastValue(v);
                    itemNameRank.setUnits(item.get("units").asText());
                    list.add(itemNameRank);
                });
            }
        }
        itemRank.setItemNameRankList(list);
        itemRank.setLastTime(SeverityUtils.getDate(new Date()));
        return itemRank;
    }

    public ItemRank getItemRank(ItemBaseDTO param) {
        ItemRank itemRank = new ItemRank();
        AssetsBaseDTO assetsBaseDTO = param.getAssetsBaseDTO();
        List<ItemNameRank> list = new ArrayList<>();
        //根据主机和监控名称批量查询lastvalue search查询
        MWZabbixAPIResult result = mwtpServerAPI.itemGetbySearch(assetsBaseDTO.getMonitorServerId(), param.getItemNames(), assetsBaseDTO.getAssetsId());
        if(result!=null){
            JsonNode itemData = (JsonNode) result.getData();
            if (itemData.size() > 0) {
                itemData.forEach(item -> {
                    ItemNameRank itemNameRank = new ItemNameRank();
                    String nameType = item.get("name").asText();
                    itemNameRank.setType(nameType.substring(1, nameType.indexOf("]")));
                    BigDecimal bigDecimal = new BigDecimal(item.get("lastvalue").asDouble());
                    double v = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    itemNameRank.setLastValue(v);
                    itemNameRank.setUnits(item.get("units").asText());
                    list.add(itemNameRank);
                });
            }
        }
        itemRank.setItemNameRankList(list);
        itemRank.setLastTime(SeverityUtils.getDate(new Date()));
        return itemRank;
    }

    public Map<String, String> getItemChName(List<String> names) {
        List<String> itemNames = new ArrayList<>();
        for (String name : names) {
            if (name.indexOf("[") != -1) {
//                中括号在前面
                if (name.indexOf("[") == 0) {
                    name = name.substring(name.indexOf("]") + 1);
                }
//                中括号在后面
                if (name.indexOf("]") == (name.length() - 1)) {
                    name = name.substring(0, name.indexOf("["));
                }
            }
            itemNames.add(name);
        }
//            去重
        HashSet h = new HashSet(itemNames);
        itemNames.clear();
        itemNames.addAll(h);
        Map<String, String> nameMap = new HashMap<>();
        List<ItemNameDto> itemChNames = itemNameDao.getItemChNames(itemNames);
        itemChNames.forEach(itemDto -> {
            nameMap.put(itemDto.getItemName(), itemDto.getDescr());
        });
        Map<String, String> map = new HashMap<>();
        String eName = "";
        String ifName = "";
        String chName = "";
        for (String name : names) {
            eName = name;
            if (name.indexOf("[") != -1) {
                ifName = name.substring(name.indexOf("["), name.indexOf("]") + 1);
//                中括号在前面
                if (name.indexOf("[") == 0) {
                    name = name.substring(name.indexOf("]") + 1);
                    chName = ifName + nameMap.get(name);
                    map.put(eName, chName);
                }
//                中括号在后面
                if (name.indexOf("]") == (name.length() - 1)) {
                    name = name.substring(0, name.indexOf("["));
                    chName = nameMap.get(name) + ifName;
                    map.put(eName, chName);
                }
            } else {
                map.put(name, nameMap.get(name));
            }
        }
        return map;
    }

    //通过itemName 获取中文名称
    public String getChName(String name) {
        String originalName = name;
        if (name != null && !"".equals(name)) {
            String ifName = "";
            String chName = "";
            if (name.indexOf("[") != -1) {
                ifName = name.substring(name.indexOf("["), name.indexOf("]") + 1);
//                中括号在前面
                if (name.indexOf("[") == 0) {
                    name = name.substring(name.indexOf("]") + 1);
                    ItemNameDto itemChName = itemNameDao.getItemChName(name);
                    if (itemChName != null) {
                        chName = ifName + itemChName.getDescr();
                        return chName;
                    }
                }
//                中括号在后面
                if (name.indexOf("]") == (name.length() - 1)) {
                    name = name.substring(0, name.indexOf("["));
                    ItemNameDto itemChName = itemNameDao.getItemChName(name);
                    if (itemChName != null) {
                        chName = itemChName.getDescr() + ifName;
                        return chName;
                    }
                }
            } else {
                ItemNameDto itemChName = itemNameDao.getItemChName(name);
                if (itemChName != null) {
                    return itemChName.getDescr();
                }
            }
        }
        return originalName;
    }


    //通过itemName 获取中文名称
    public String getChNameByMap(String name, Map<String, String> mapInfo) {
        String originalName = name;
        if (name != null && !"".equals(name)) {
            String ifName = "";
            String chName = "";
            if (name.indexOf("[") != -1) {
                ifName = name.substring(name.indexOf("["), name.indexOf("]") + 1);
//                中括号在前面
                if (name.indexOf("[") == 0) {
                    name = name.substring(name.indexOf("]") + 1);
                    String descr = mapInfo.get(name);
                    if (!Strings.isNullOrEmpty(descr)) {
                        chName = ifName + descr;
                        return chName;
                    }
                }
//                中括号在后面
                if (name.indexOf("]") == (name.length() - 1)) {
                    name = name.substring(0, name.indexOf("["));
                    String descr = mapInfo.get(name);
                    if (!Strings.isNullOrEmpty(descr)) {
                        chName = descr + ifName;
                        return chName;
                    }
                }
            } else {
                String descr = mapInfo.get(name);
                if (!Strings.isNullOrEmpty(descr)) {
                    return descr;
                }
            }
        }
        return originalName;
    }


    public Map<String, String> getitemChNameMap() {
        Map map = new HashMap();
        List<Map<String, String>> itemChNameAllInfo = itemNameDao.getItemChNameAllInfo();
        for (Map<String, String> m : itemChNameAllInfo) {
            map.put(m.get("name"), m.get("ChName"));
        }
        return map;
    }

    //将name中的[]命名去掉
    public String getChNameWithout(String name) {
        if (name != null && !"".equals(name)) {
            if (name.indexOf("[") != -1) {
//                中括号在前面
                if (name.indexOf("[") == 0) {
                    name = name.substring(name.indexOf("]") + 1);
                }
//                中括号在后面
                if (name.indexOf("]") == (name.length() - 1)) {
                    name = name.substring(0, name.indexOf("["));
                }
            }
            ItemNameDto itemChName = itemNameDao.getItemChName(name);
            if (itemChName != null) {
                return itemChName.getDescr();
            } else {
                return name;
            }
        }
        return null;
    }

    //通过itemName 获取中文名称
    public String getChNameBase(String name) {
        String originalName = name;
        if (name != null && !"".equals(name)) {
            ItemNameDto itemChName = itemNameDao.getItemChName(name);
            if (itemChName != null) {
                return itemChName.getDescr();
            }
        }
        return originalName;
    }

    /**
     * 根据带外ip获取带外资产主机id
     *
     * @param outBandIp
     * @return
     */
    public Boolean getIsHaveMonitorInfo(String outBandIp) {
        if (outBandIp != null && StringUtils.isNotEmpty(outBandIp)) {
            AssetsBaseDTO dto = tangibleOutbandDao.getOutHostId(outBandIp);
            if (dto != null && null != dto.getAssetsId() && StringUtils.isNotEmpty(dto.getAssetsId())) {
                //通过主机获取带外资产监控信息
                MWZabbixAPIResult result = mwtpServerAPI.getApplication(dto.getMonitorServerId(), dto.getAssetsId());
                if (result!=null && !result.isFail()) {
                    List<ApplicationDTO> lists = resultResolver.analysisResult(mwtpServerAPI.getServerType(dto.getMonitorServerId()),
                            String.valueOf(result.getData()));
                    if (lists.size() > 0) {
                        //如果有监控信息
                        return true;
                    }
                }
            }
        }
        return false;
    }

    //根据item中valuemapid属性，转换成对应的value值
    public String getValueMapById(int monitorServerId, String valuemapId, String value) {
        List<String> valuemapIds = new ArrayList<>();
        valuemapIds.add(valuemapId);
        MWZabbixAPIResult valueMapById = mwtpServerAPI.getValueMapById(monitorServerId, valuemapIds);
        if (null != valueMapById && valueMapById.getCode() == 0) {
            List<ValuemapDto> dataList = JSONArray.parseArray(String.valueOf(valueMapById.getData()), ValuemapDto.class);
            if (dataList.size() > 0) {
                List<ValueMappingDto> mappings = dataList.get(0).getMappings();
                for (ValueMappingDto mapping : mappings) {
                    if (null != value && StringUtils.isNotEmpty(value)) {
                        if (value.equals(mapping.getValue())) {
                            return mapping.getNewvalue();
                        }
                    }
                }
            }
        }
        return null;
    }

    //根据item中valuemapid属性，转换成对应的value值
    public Map<String, Map> getValueMapByIdList(int monitorServerId, List<String> valuemapId) {
        MWZabbixAPIResult valueMapById = mwtpServerAPI.getValueMapById(monitorServerId, valuemapId);
        List<ValuemapDto> dataList = new ArrayList<>();
        if (null != valueMapById && valueMapById.getCode() == 0) {
            dataList = JSONArray.parseArray(String.valueOf(valueMapById.getData()), ValuemapDto.class);
        }
        Map<String, Map> map = new HashMap();
        for (ValuemapDto dto : dataList) {
            Map map1 = new HashMap();
            for (ValueMappingDto val : dto.getMappings()) {
                val.getValue();
                val.getNewvalue();
                map1.put(val.getValue(), val.getNewvalue());
            }
            map.put(dto.getValuemapid(), map1);
        }
        return map;
    }

    //    获取item的历史数据，取某段时间内的value的平均值，最大值，最小值   timeType:1——15分钟；2——一小时
    public RedisItemHistoryDto getHistoryByTime(int monitorServerId, String itemId, Integer timeType, Integer type) {
        Long startTime = 0L;
        Calendar calendar = Calendar.getInstance();
        Long endTime = calendar.getTimeInMillis() / 1000L;
        switch (timeType) {
            case 1:
                calendar.add(Calendar.MINUTE, -15);
                startTime = calendar.getTimeInMillis() / 1000L;
                break;
            case 2:
                calendar.add(Calendar.HOUR, -1);
                startTime = calendar.getTimeInMillis() / 1000L;
                break;
            default:
                break;
        }
        MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.HistoryGetByTimeAndType(monitorServerId, itemId, startTime, endTime, type);
        List<BigDecimal> list = new ArrayList<>();
        BigDecimal sum = new BigDecimal("0");
        Boolean flag = true;
        if(mwZabbixAPIResult!=null){
            JsonNode resultData = (JsonNode) mwZabbixAPIResult.getData();
            if (resultData.size() > 0) {
                for (JsonNode result : resultData) {
                    try {
                        BigDecimal value = new BigDecimal(result.get("value").asText());
                        list.add(value);
                        sum = sum.add(value);
                    } catch (Exception e) {
                        flag = false;
                        log.error("fail to new BigDecimal  cause:{}", e.getMessage());
                    }
                }
                if (flag) {
                    RedisItemHistoryDto redisHistoryDto = new RedisItemHistoryDto();
                    String min = Collections.min(list).toString();
                    String max = Collections.max(list).toString();
                    BigDecimal size = new BigDecimal(resultData.size());
                    String avg = sum.divide(size, 2, BigDecimal.ROUND_HALF_UP).toString();
                    redisHistoryDto.setMinValue(min);
                    redisHistoryDto.setMaxValue(max);
                    redisHistoryDto.setAvgValue(avg);
                    redisHistoryDto.setMinMax(Collections.min(list).doubleValue());
                    redisHistoryDto.setMaxMax(Collections.max(list).doubleValue());
                    redisHistoryDto.setAvgMax(sum.divide(size, 2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    redisHistoryDto.setUpdateTime(SeverityUtils.getDate(new Date()));
                    return redisHistoryDto;
                }
            }
        }

        return null;
    }

    /**
     * 比较平均、最大、最小的值，返回最大的
     *
     * @param redisValue
     * @param zabbixValue
     * @return
     */
    public RedisItemHistoryDto compareValue(RedisItemHistoryDto redisValue, RedisItemHistoryDto zabbixValue) {
        if (redisValue != null && zabbixValue != null) {
            if (redisValue.getAvgMax() > zabbixValue.getAvgMax()) {
                zabbixValue.setAvgMax(redisValue.getAvgMax());
            }
            if (redisValue.getMaxMax() > zabbixValue.getMaxMax()) {
                zabbixValue.setMaxMax(redisValue.getMaxMax());
            }
            if (redisValue.getMinMax() > zabbixValue.getMinMax()) {
                zabbixValue.setMinMax(redisValue.getMinMax());
            }
        }
        if (zabbixValue == null && redisValue != null) {
            zabbixValue = new RedisItemHistoryDto();
            zabbixValue.setMaxMax(redisValue.getMaxMax());
            zabbixValue.setAvgMax(redisValue.getAvgMax());
            zabbixValue.setMinMax(redisValue.getMinMax());
        }
        return zabbixValue;
    }

    /**
     * 根据主机id获取主机性能指标信息
     *
     * @param monitorServerId
     * @param hostId
     * @return
     */
    public HostPerformanceInfoDto getHostPerformanceInfo(int monitorServerId, String hostId) {
        HostPerformanceInfoDto hostFunctionDto = new HostPerformanceInfoDto();
        if (null != hostId && StringUtils.isNotEmpty(hostId)) {
            List<String> itemNames = Arrays.asList(ZbxConstants.CPU_UTILIZATION
                    , ZbxConstants.MEMORY_UTILIZATION, ZbxConstants.ICMP_RESPONSE_TIME);

            MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.itemGetbyNameList(monitorServerId, itemNames, hostId
                    ,false ,true);
            if(mwZabbixAPIResult!=null){
                JsonNode itemData = (JsonNode) mwZabbixAPIResult.getData();
                if (itemData.size() > 0) {
                    Integer cCount = 0;
                    Double cUtilization = 0.0;
                    Integer mCount = 0;
                    Double mUtilization = 0.0;
                    for (JsonNode item : itemData) {
                        String name = item.get("name").asText();
                        Matcher matcherCpu = ZbxConstants.CPUPattern.matcher(name);
                        if (matcherCpu.find()) {
                            cCount++;
                            cUtilization += item.get("lastvalue").asDouble();
                        }

                        if (name.indexOf(ZbxConstants.MEMORY_UTILIZATION) != -1) {
                            mCount++;
                            mUtilization += item.get("lastvalue").asDouble();
                        } else if (name.indexOf(ZbxConstants.ICMP_RESPONSE_TIME) != -1) {
                            hostFunctionDto.setDelayed(UnitsUtil.getValueWithUnits(item.get("lastvalue").asText(), item.get("units").asText()));
                        }
                    }
                    if (cCount != 0) {
                        hostFunctionDto.setCpuUnitilization(UnitsUtil.getValueWithUnits(String.valueOf(cUtilization / cCount), "%"));
                        hostFunctionDto.setCpuUTIL(cUtilization / cCount);
                    }else{
                        hostFunctionDto.setCpuUnitilization(ZbxConstants.NO_DATA);
                    }

                    if (mCount != 0) {
                        hostFunctionDto.setMemoryUtilization(UnitsUtil.getValueWithUnits(String.valueOf(mUtilization / mCount), "%"));
                        hostFunctionDto.setMemoryUTIL(mUtilization / mCount);
                    }else{
                        hostFunctionDto.setMemoryUtilization(ZbxConstants.NO_DATA);
                    }
                }
            }

        }
        return hostFunctionDto;
    }

    /**
     * 根据主机id和接口名称获取接口性能信息
     *
     * @param monitorServerId
     * @param hostId
     * @param interfaceName
     * @return
     */
    public HostPerformanceInfoDto getHostPerformanceInfo(int monitorServerId, String hostId, String interfaceName) {
        List<String> hostids = new ArrayList<>();
        hostids.add(hostId);
        List<String> ifNames = new ArrayList<>();
        ifNames.add(interfaceName);

        HostPerformanceInfoDto hostFunctionDto = null;
        List<HostPerformanceInfoDto> hostList = getHostPerformanceInfo(monitorServerId ,hostids ,ifNames);
        if(hostList.size() > 0){
            hostFunctionDto = hostList.get(0);
        }
        return hostFunctionDto;
    }

    /**
     * 根据主机id和接口名称获取接口性能信息
     *
     * @param monitorServerId
     * @param hostIds
     * @param interfaceNames
     * @return
     */
    public List<HostPerformanceInfoDto> getHostPerformanceInfo(int monitorServerId, List<String> hostIds, List<String> interfaceNames) {

        Map<String ,HostPerformanceInfoDto> hostMap = new HashMap<>();
        List<String> nameList = new ArrayList<>();
        for(String name : interfaceNames){
            List<String> ifNameList = Arrays.asList("[" + name + "]" + ZbxConstants.MW_INTERFACE_IN_TRAFFIC
                    , "[" + name + "]" + ZbxConstants.MW_INTERFACE_OUT_TRAFFIC
                    ,"[" + name + "]" + ZbxConstants.MW_INTERFACE_IN_DROPPED
                    ,"[" + name + "]" + ZbxConstants.MW_INTERFACE_OUT_DROPPED
                    ,"[" + name + "]" + ZbxConstants.MW_INTERFACE_IN_UTILIZATION
                    ,"[" + name + "]" + ZbxConstants.MW_INTERFACE_OUT_UTILIZATION
            );
            nameList.addAll(ifNameList);
        }
        String seq = "-";
        if (null != hostIds && hostIds.size() > 0) {
            MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.itemGetbyFilter(monitorServerId, nameList, hostIds);
            if(mwZabbixAPIResult!=null){
                JsonNode itemData = (JsonNode) mwZabbixAPIResult.getData();
                Map<String ,HostIfPerformanceInfo> hostIfMap = new HashMap<>();
                if (itemData.size() > 0) {
                    for (JsonNode item : itemData) {
                        String hostid = item.get("hostid").asText();
                        String name = item.get("name").asText();
                        String ifName = "";

                        for(Pattern pattern : ifPatternList){
                            Matcher matcher = pattern.matcher(name);
                            if(matcher.find()){
                                ifName = matcher.group(1);
                                String key = hostid + seq + ifName;
                                HostIfPerformanceInfo hostIfPerformanceInfo = hostIfMap.get(key);
                                if(null == hostIfPerformanceInfo){
                                    hostIfPerformanceInfo = new HostIfPerformanceInfo();
                                    hostIfPerformanceInfo.setIfName(ifName);
                                    hostIfMap.put(key ,hostIfPerformanceInfo);
                                }
                                HostIfPerformanceInfoCallback dataset = ifCallbackMap.get(pattern);
                                dataset.callback(hostIfPerformanceInfo, item);
                                break;
                            }
                        }
                    }
                }

                for(String key : hostIfMap.keySet()){
                    String hostid = key.split(seq)[0];
                    HostPerformanceInfoDto hostPerformanceInfoDto = hostMap.get(hostid);
                    if(null == hostPerformanceInfoDto){
                        hostPerformanceInfoDto = new HostPerformanceInfoDto();
                        hostPerformanceInfoDto.setHostId(hostid);
                        hostMap.put(hostid ,hostPerformanceInfoDto);
                    }
                    HostIfPerformanceInfo hostIfPerformanceInfo = hostIfMap.get(key);
                    if(null != hostIfPerformanceInfo){
                        hostPerformanceInfoDto.addHostIfPerformanceInfo(hostIfPerformanceInfo);
                    }
                }
            }


        }

        return new ArrayList<>(hostMap.values());
    }

    /**
     * //根据info应用集 获取所有名字集合
     *
     * @param monitorServerId
     * @param hostId
     * @param applicationName
     * @param itemName
     * @param hasDescription  是否含有描述信息
     * @return
     */
    public List getNames(int monitorServerId, String hostId, String applicationName, String itemName, boolean hasDescription) {
        String otherCase = itemName;
        String otherApplication = applicationName;
        //当应用集属于接口时才存在是否含有描述信息的情况
        boolean flag = hasDescription && ZabbixItemConstant.INTERFACES_INFO.equals(applicationName);
        itemName = flag ? ZabbixItemConstant.INTERFACE_DESCR : itemName;
        applicationName = flag ? ZabbixItemConstant.INTERFACES : applicationName;
        List nameList = new ArrayList<>();
        if (null != hostId && StringUtils.isNotEmpty(hostId)) {
            MWZabbixAPIResult result = (applicationName != null && !"".equals(applicationName)) ? mwtpServerAPI.getItemDataByAppName(monitorServerId, hostId, applicationName, itemName)
                    : mwtpServerAPI.itemsGet(monitorServerId, hostId, itemName);
            if(result!=null){
                JsonNode resultData = (JsonNode) result.getData();
                if (result.getCode() == 0 && resultData.size() > 0) {
                    resultData.forEach(item -> {
                        String name = item.get("name").asText();
                        if (name.indexOf("[") != -1) {
                            name = name.substring(name.indexOf("[") + 1, name.indexOf("]"));
                            if (flag) {
                                DropDownNamesDesc drop = DropDownNamesDesc.builder()
                                        .name(name)
                                        .description(item.get("lastvalue").asText()).build();
                                nameList.add(drop);
                            } else {
                                nameList.add(name);
                            }
                        }
                    });
                } else {//存在一种情况，zabbix模板中的接口信息中没有接口描述的监控项,只能通过其他监控项名称获取
                    if (flag) {
                        MWZabbixAPIResult otherResult = mwtpServerAPI.getItemDataByAppName(monitorServerId, hostId, otherApplication, otherCase);
                        if(otherResult!=null){
                            JsonNode otherResultData = (JsonNode) otherResult.getData();
                            if (otherResult.getCode() == 0 && otherResultData.size() > 0) {
                                otherResultData.forEach(item -> {
                                    String name = item.get("name").asText();
                                    if (name.indexOf("[") != -1) {
                                        name = name.substring(name.indexOf("[") + 1, name.indexOf("]"));
                                        DropDownNamesDesc drop = DropDownNamesDesc.builder()
                                                .name(name).build();
                                        nameList.add(drop);
                                    }
                                });
                            }
                        }
                    }
                }
            }


        }
        return nameList;
    }

    public QueryAssetsAvailableParam getItemIdByAvailableItem(QueryAssetsAvailableParam param) {
        String itemId = "";
        Integer value_type = 0;
        String hostId = "";
        MWZabbixAPIResult result = mwtpServerAPI.itemGetbyType(param.getMonitorServerId(), ZabbixItemConstant.MW_HOST_AVAILABLE, param.getAssetsId(), true);
        if (result!=null && result.getData()!=null ) {
            JsonNode node = (JsonNode) result.getData();
            if(node.size()>0){
                itemId = node.get(0).get("itemid").asText();
                value_type = node.get(0).get("value_type").asInt();
                hostId = node.get(0).get("hostid").asText();
            }
            else {//说明当前主机可能是应用/中间件之类，需从相同ip的主机中找可用性
                Map<Integer, List<String>> assetsIds = new HashMap<>();//可能存在在别的zabbix中监控的主机
                List<MwTangibleassetsDTO> tangibleassetsDTOList = tangibleOutbandDao.checkIpAddress(param.getInBandIp());
                logger.error("getItemIdByAvailableItem with tangibleassetsDTOList={}", tangibleassetsDTOList);
                if (tangibleassetsDTOList != null && tangibleassetsDTOList.size() > 0) {
                    tangibleassetsDTOList.forEach(assets -> {
                        if (!param.getId().equals(assets.getId())) {
                            List<String> assetsList = assetsIds.get(assets.getMonitorServerId());
                            logger.error("getItemIdByAvailableItem with assetsList={}", assetsList);
                            if (assetsList != null && assetsList.size() > 0) {
                                assetsList.add(assets.getAssetsId());
                                assetsIds.put(assets.getMonitorServerId(), assetsList);
                            } else {
                                assetsIds.put(assets.getMonitorServerId(), Arrays.asList(assets.getAssetsId()));
                            }
                        }
                    });
                }
                for (Integer key : assetsIds.keySet()) {
                    MWZabbixAPIResult resultList = mwtpServerAPI.itemGetbyFilter(key, ZabbixItemConstant.MW_HOST_AVAILABLE, assetsIds.get(key));
                    if(resultList!=null){
                        JsonNode nodeList = (JsonNode) resultList.getData();
                        if (nodeList.size() > 0) {
                            itemId = nodeList.get(0).get("itemid").asText();
                            value_type = nodeList.get(0).get("value_type").asInt();
                            hostId = node.get(0).get("hostid").asText();
                            param.setMonitorServerId(key);
                        }
                    }
                }
            }
        }
        param.setItemId(itemId);
        param.setAssetsId(hostId);
        param.setValue_type(value_type);
        return param;
    }


    public Map<Integer, Map<Integer, List<String>>> getListitemsByassetId(List<QueryAssetsAvailableParam> mwTangibleassetsTables, Map<String, List<String>> map) {
        Map<Integer, Map<Integer, List<String>>> fenzu = new HashMap<>();
        Map<Integer, List<String>> mapk = new HashMap<>();
        for (QueryAssetsAvailableParam l : mwTangibleassetsTables) {
            if (mapk.get(l.getMonitorServerId()) == null) {
                List<String> list = new ArrayList<>();
                list.add(l.getAssetsId());
                mapk.put(l.getMonitorServerId(), list);
            } else {
                List<String> list = mapk.get(l.getMonitorServerId());
                list.add(l.getAssetsId());
                mapk.put(l.getMonitorServerId(), list);
            }
        }

        for (Integer s : mapk.keySet()) {
            MWZabbixAPIResult result = mwtpServerAPI.itemGetbyType(s, ZabbixItemConstant.MW_HOST_AVAILABLE, mapk.get(s), true);
            if(result!=null){
                log.error("导致错误原因", s.toString()+":"+ZabbixItemConstant.MW_HOST_AVAILABLE+":"+mapk.get(s).toString()+result.toString());
                JsonNode resultData = (JsonNode) result.getData();
//            itemId = resultData.get(i).get("itemid").asText();
//            value_type = resultData.get(i).get("value_type").asInt();
//            hostId = resultData.get(i).get("hostid").asText();
                for (int i = 0; i < resultData.size(); i++) {
                    if (resultData.get(i).get("hostid") == null) {
                        List<String> k = map.get(resultData.get(i).get("hostid").asText());
                        k.add(resultData.get(i).get("itemid").asText());
                        map.put(resultData.get(i).get("hostid").asText(), k);
                    } else {
                        List<String> k = new ArrayList<>();
                        if (resultData.get(i).get("itemid").asText() != null && resultData.get(i).get("itemid").asText().trim() != "") {
                            k.add(resultData.get(i).get("itemid").asText());
                            map.put(resultData.get(i).get("hostid").asText(), k);
                        }
                    }
                    if (fenzu.get(s) != null) {
                        if (fenzu.get(s).get(resultData.get(i).get("value_type").asInt()) == null) {
                            List<String> k = new ArrayList<>();
                            k.add(resultData.get(i).get("itemid").asText());
                            Map<Integer, List<String>> listMap = fenzu.get(s);
                            listMap.put(resultData.get(i).get("value_type").asInt(), k);
                            fenzu.put(s, listMap);
                        } else {
                            List<String> k = fenzu.get(s).get(resultData.get(i).get("value_type").asInt());
                            k.add(resultData.get(i).get("itemid").asText());
                            Map<Integer, List<String>> listMap = fenzu.get(s);
                            listMap.put(resultData.get(i).get("value_type").asInt(), k);
                            fenzu.put(s, listMap);
                        }
                    } else {
                        List<String> k = new ArrayList<>();
                        k.add(resultData.get(i).get("itemid").asText());
                        Map<Integer, List<String>> listMap = new HashMap<>();
                        listMap.put(resultData.get(i).get("value_type").asInt(), k);
                        fenzu.put(s, listMap);
                    }
                }
            }
        }


        return fenzu;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ifPatternList = new ArrayList<>();
        ifCallbackMap = new HashMap<>();
        Pattern pattern = Pattern.compile("^\\[(.+)\\]" + ZbxConstants.MW_INTERFACE_IN_TRAFFIC+"$");
        ifPatternList.add(pattern);
        ifCallbackMap.put(pattern , new HostIfPerformanceInfoCallback(){
            @Override
            public void callback(HostIfPerformanceInfo hostIfPerformanceInfo ,JsonNode item) {
                hostIfPerformanceInfo.setInBps(UnitsUtil.getValueWithUnits(item.get("lastvalue").asText(), item.get("units").asText()));
            }
        });

        pattern = Pattern.compile("^\\[(.+)\\]" + ZbxConstants.MW_INTERFACE_OUT_TRAFFIC+"$");
        ifPatternList.add(pattern);
        ifCallbackMap.put(pattern , new HostIfPerformanceInfoCallback(){
            @Override
            public void callback(HostIfPerformanceInfo hostIfPerformanceInfo ,JsonNode item) {
                hostIfPerformanceInfo.setOutBps(UnitsUtil.getValueWithUnits(item.get("lastvalue").asText(), item.get("units").asText()));
            }
        });

        pattern = Pattern.compile("^\\[(.+)\\]" + ZbxConstants.MW_INTERFACE_IN_DROPPED+"$");
        ifPatternList.add(pattern);
        ifCallbackMap.put(pattern , new HostIfPerformanceInfoCallback(){
            @Override
            public void callback(HostIfPerformanceInfo hostIfPerformanceInfo ,JsonNode item) {
                hostIfPerformanceInfo.setPickDropped(UnitsUtil.getValueWithUnits(item.get("lastvalue").asText(), item.get("units").asText()));
            }
        });

        pattern = Pattern.compile("^\\[(.+)\\]" + ZbxConstants.MW_INTERFACE_OUT_DROPPED+"$");
        ifPatternList.add(pattern);
        ifCallbackMap.put(pattern , new HostIfPerformanceInfoCallback(){
            @Override
            public void callback(HostIfPerformanceInfo hostIfPerformanceInfo ,JsonNode item) {
                hostIfPerformanceInfo.setSendDropped(UnitsUtil.getValueWithUnits(item.get("lastvalue").asText(), item.get("units").asText()));
            }
        });

        pattern = Pattern.compile("^\\[(.+)\\]" + ZbxConstants.MW_INTERFACE_IN_UTILIZATION+"$");
        ifPatternList.add(pattern);
        ifCallbackMap.put(pattern , new HostIfPerformanceInfoCallback(){
            @Override
            public void callback(HostIfPerformanceInfo hostIfPerformanceInfo ,JsonNode item) {
                hostIfPerformanceInfo.setInUtilization(UnitsUtil.getValueWithUnits(item.get("lastvalue").asText(), item.get("units").asText()));
            }
        });

        pattern = Pattern.compile("^\\[(.+)\\]" + ZbxConstants.MW_INTERFACE_OUT_UTILIZATION+"$");
        ifPatternList.add(pattern);
        ifCallbackMap.put(pattern , new HostIfPerformanceInfoCallback(){
            @Override
            public void callback(HostIfPerformanceInfo hostIfPerformanceInfo ,JsonNode item) {
                hostIfPerformanceInfo.setOutUtilization(UnitsUtil.getValueWithUnits(item.get("lastvalue").asText(), item.get("units").asText()));
            }
        });
    }
}
