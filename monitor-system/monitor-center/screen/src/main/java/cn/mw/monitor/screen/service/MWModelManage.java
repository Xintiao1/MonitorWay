package cn.mw.monitor.screen.service;

import cn.mw.monitor.alert.service.manager.MWAlertManager;
import cn.mw.monitor.api.common.UuidUtil;
import cn.mw.monitor.link.dao.MWNetWorkLinkDao;
import cn.mw.monitor.link.dto.NetWorkLinkDto;
import cn.mw.monitor.link.param.LinkDropDownParam;
import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.screen.constant.ScreenConstant;
import cn.mw.monitor.screen.dao.MWIndexDao;
import cn.mw.monitor.screen.dao.MWLagerScreenDao;
import cn.mw.monitor.screen.dto.*;
import cn.mw.monitor.screen.model.FilterAssetsParam;
import cn.mw.monitor.screen.model.IndexBulk;
import cn.mw.monitor.screen.model.IndexModelBase;
import cn.mw.monitor.screen.model.ModelBaseTable;
import cn.mw.monitor.service.MWNetWorkLinkService;
import cn.mw.monitor.service.alert.api.MWAlertService;
import cn.mw.monitor.service.alert.dto.AssetsDto;
import cn.mw.monitor.service.alert.dto.ZbxAlertDto;
import cn.mw.monitor.service.assets.model.*;
import cn.mw.monitor.service.assets.param.QueryTangAssetsParam;
import cn.mw.monitor.service.impl.MWLinkZabbixManger;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.service.server.api.MwServerService;
import cn.mw.monitor.service.server.api.dto.MWItemHistoryDto;
import cn.mw.monitor.service.server.api.dto.ServerHistoryDto;
import cn.mw.monitor.service.user.api.OrgModuleType;
import cn.mw.monitor.service.zbx.param.AlertParam;
import cn.mw.monitor.state.DataType;
import cn.mw.monitor.user.dto.GlobalUserInfo;
import cn.mw.monitor.user.service.MWOrgService;
import cn.mw.monitor.user.service.MWUserService;
import cn.mw.monitor.util.*;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mw.zbx.common.ZbxConstants;
import cn.mw.zbx.manger.MWWebZabbixManger;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.pagehelper.PageInfo;
import org.apache.commons.beanutils.PropertyUtils;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.cluster.metadata.AliasMetadata;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author xhy
 * @date 2020/4/13 15:22
 */
@Component
public class MWModelManage {
    private static final Logger log = LoggerFactory.getLogger("MWIndexController");

    public final static String TIMESTAMP = "@timestamp";


    @Autowired
    private MWAlertManager alertManager;

    @Autowired
    private MwModelViewCommonService mwModelViewCommonService;

    @Autowired
    private MWWebZabbixManger mwWebZabbixManger;
    @Resource
    private MWLagerScreenDao dao;
    @Resource
    private MWIndexDao indexDao;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private MwAssetsManager mwAssetsManager;

    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    @Autowired
    private MWNetWorkLinkService mwNetWorkLinkService;

    @Autowired
    private MWLinkZabbixManger mwLinkZabbixManger;
    @Value("${screen.debug}")
    boolean debug;

    @Autowired
    private MwServerService serverService;

    @Resource
    MWNetWorkLinkDao mwNetWorkLinkDao;

    @Autowired
    private MWUserService userService;

    @Autowired
    private MWOrgService orgService;

    @Value("${model.assets.enable}")
    private boolean modelAssetEnable;

    //获得所有的资产统计
    public List<AlertPriorityType> hostgroupListGetByName(Integer userId) {
        String moduleType = MWUtils.MODEL_ALARM_COUNT_TYPE;
        List<ModelBaseTable> modelBaseTables = dao.getModelAssetsTypeId(moduleType);
        List<AlertPriorityType> lists = new ArrayList<>();
        for (ModelBaseTable modelBaseTable : modelBaseTables) {
            if(StringUtils.isBlank(modelBaseTable.getModelContent())){
                continue;
            }
            Integer assetsTypeId = Integer.valueOf(modelBaseTable.getModelContent());
            AlertPriorityType alertPriorityType = new AlertPriorityType();
            alertPriorityType.setGroupName(SeverityUtils.TYPE2ZHN.get(assetsTypeId));
            FilterAssetsParam filterAssetsParam = FilterAssetsParam.builder().modelId(modelBaseTable.getId()).userId(userId).type(DataType.INDEX.getName()).assetsTypeId(assetsTypeId).build();

            Map<Integer, List<String>> map = getAssetIdsByServerId(filterAssetsParam);
            if (null != map && map.size() > 0) {
                for (Integer key : map.keySet()) {
                    log.info("首页serverid:" + key);
                    List<String> hostIds = map.get(key);
                    log.info("首页hostIds:" + hostIds.toString());
                    if (hostIds.size() > 0) {
                        alertPriorityType.setSum((null == alertPriorityType.getSum() ? 0 : alertPriorityType.getSum()) + hostIds.size());
                        MWZabbixAPIResult triggerList = mwtpServerAPI.problemget(key, hostIds);
                        JsonNode triggerData = (JsonNode) triggerList.getData();
                        log.info("triggerData{}", triggerData);
                        alertPriorityType.setCount((null == alertPriorityType.getCount() ? 0 : alertPriorityType.getCount()) + triggerData.size());
                    } else {
                        alertPriorityType.setSum(0);
                        alertPriorityType.setCount2(0);
                        alertPriorityType.setCount3(0);
                        alertPriorityType.setCount4(0);
                        alertPriorityType.setCount5(0);
                    }
                }
            } else {
                alertPriorityType.setSum(0);
                alertPriorityType.setCount2(0);
                alertPriorityType.setCount3(0);
                alertPriorityType.setCount4(0);
                alertPriorityType.setCount5(0);
            }
            lists.add(alertPriorityType);

        }
        return lists;

    }

    public List<Map<String,List<Integer>>> getAsseTable(QueryTangAssetsParam qparam){
        List<Map<String,List<Integer>>> list=new ArrayList<>();
        Map<Integer,List<MwTangibleassetsTable>> listMap=new HashMap<>();
        List<MwTangibleassetsTable> assetsTable = mwAssetsManager.getAssetsTable(qparam);
        if(assetsTable.size()>0){
            List<MwTangibleassetsTable> collect = assetsTable.stream().filter(f -> f.getAssetsTypeId()==6).collect(Collectors.toList());
            if(collect.size()>0){
                listMap = collect.stream().collect(Collectors.groupingBy(MwTangibleassetsTable::getAssetsTypeSubId));
            }
            if(listMap.size()>0){
                for(List<MwTangibleassetsTable> v:listMap.values()){
                    String assetsTypeSubName = v.get(0).getAssetsTypeSubName();
                    if(assetsTypeSubName==null){
                        assetsTypeSubName="unknown";
                    }
                    int normal = (int)v.stream().filter(f -> "NORMAL".equals(f.getItemAssetsStatus())).count();
                    int total = v.size();
                    int abnormal =total-normal;
                    List<Integer> integers = Arrays.asList(total, normal, abnormal);
                    Map<String,List<Integer>> map=new HashMap<>();
                    map.put(assetsTypeSubName,integers);
                    list.add(map);
                }
            }
        }
        return list;
    }

    public LinkRankDto getLinkRankDto(NetWorkLinkDto dto){
        LinkRankDto linkRankDto=new LinkRankDto();
        linkRankDto.setLinkName(dto.getLinkName());
        String port = "";
        String bandHostid = "";
        Integer bandServerId = 0;
        if (dto.getValuePort().equals("ROOT")) {
            bandHostid = dto.getRootAssetsParam().getAssetsId();
            bandServerId = dto.getRootAssetsParam().getMonitorServerId();
            port = dto.getRootPort();
        } else {
            bandHostid = dto.getTargetAssetsParam().getAssetsId();
            bandServerId = dto.getTargetAssetsParam().getMonitorServerId();
            port = dto.getTargetPort();
        }
        List<String> bandNameList = new ArrayList<>();
        // bandNameList.add("[" + port + "]" + "INTERFACE_BANDWIDTH");
        bandNameList.add("[" + port + "]" + "MW_INTERFACE_IN_TRAFFIC");
        bandNameList.add("[" + port + "]" + "MW_INTERFACE_OUT_TRAFFIC");

        Map<String, Object> map = mwLinkZabbixManger.getItemValue(dto);
        if (null != map) {
            if ("DISACTIVE".equals(dto.getEnable())) {
                linkRankDto.setStatus(map.get("MW_INTERFACE_STATUS").toString());
            } else {
                linkRankDto.setStatus(null != map.get("ICMP_PING") ? map.get("ICMP_PING").toString() : "");
            }
        }
        //查询zabbix获得流入流出流量的itemid
        MWZabbixAPIResult result = mwtpServerAPI.itemGetbyFilter(bandServerId, bandNameList, bandHostid);

        if (result.getCode() == 0) {
            JsonNode jsonNode = (JsonNode) result.getData();
            String bandUnit = dto.getBandUnit();

            if (jsonNode.size() > 0) {
                for (JsonNode node : jsonNode) {
                    String name = node.get("name").asText();
                    name = name.substring(name.indexOf("]") + 1, name.length());
                    String lastValue = node.get("lastvalue").asText();
                    switch (name){
                        case "MW_INTERFACE_IN_TRAFFIC":
                            String units = node.get("units").asText();
                            linkRankDto.setInValue(UnitsUtil.getValueWithUnits(lastValue,units));
                            break;
                        case "MW_INTERFACE_OUT_TRAFFIC":
                            String ounits = node.get("units").asText();
                            linkRankDto.setOutValue(UnitsUtil.getValueWithUnits(lastValue,ounits));
                            break;
                    }
                }
            }
        }
        return linkRankDto;
    }
    
    public List<LinkRankDto> getLinkRank(String linkInterfaces,Integer userId){
        List<LinkRankDto> linkRankDtos=new ArrayList<>();
        String substring = linkInterfaces.substring(1, linkInterfaces.lastIndexOf("]"));
        ////System.out.println(substring);
        String[] split = substring.split(",");
        List<String> linkList=new ArrayList<>();
        for (String s : split) {
            linkList.add(s.trim());
        }
        LinkDropDownParam linkDropDownParam = new LinkDropDownParam();
        linkDropDownParam.setLinkIds(linkList);
        linkDropDownParam.setIsAdvancedQuery(false);
        linkDropDownParam.setIsFilterQuery(true);
        linkDropDownParam.setUserId(userId);
        linkDropDownParam.setPageSize(Integer.MAX_VALUE);
        //调用我的监控线路接口查询所有，最新逻辑
        Reply reply = mwNetWorkLinkService.selectList(linkDropDownParam);
        if(null != reply && reply.getRes() == PaasConstant.RES_SUCCESS){
            PageInfo pageInfo = (PageInfo) reply.getData();
            List<NetWorkLinkDto> list = pageInfo.getList();
            for (NetWorkLinkDto nlt : list) {
                LinkRankDto rankDto = LinkRankDto.builder().status(nlt.getStatus()).linkName(nlt.getLinkName()).
                        inValue(String.valueOf(nlt.getInLinkBandwidthUtilization())).
                        outValue(String.valueOf(nlt.getOutLinkBandwidthUtilization())).build();
                linkRankDtos.add(rankDto);
            }
            return linkRankDtos;
        }
        //第一版逻辑
        List<NetWorkLinkDto> netWorkLinkDtos = mwNetWorkLinkService.getNetWorkLinkDtos(linkDropDownParam);
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        List<Future<LinkRankDto>> futureList = new ArrayList<>();
        for (NetWorkLinkDto netWorkLinkDto : netWorkLinkDtos) {
            GetDataByCallable<LinkRankDto> getDataByCallable=new GetDataByCallable<LinkRankDto>() {
                @Override
                public LinkRankDto call() throws Exception {
                    return getLinkRankDto(netWorkLinkDto);
                }
            };
            if(null!=getDataByCallable){
                Future<LinkRankDto> submit = executorService.submit(getDataByCallable);
                futureList.add(submit);
            }
        }
        for (Future<LinkRankDto> linkRankDtoFuture : futureList) {
            try {
                LinkRankDto linkRankDto = linkRankDtoFuture.get(30, TimeUnit.SECONDS);
                linkRankDtos.add(linkRankDto);
            } catch (Exception e) {
                linkRankDtoFuture.cancel(true);
                executorService.shutdown();
            }
        }
        executorService.shutdown();
        return linkRankDtos;
    }

    public Map<Integer,Map<String,Integer>>  getAssetByOrg(QueryTangAssetsParam qparam){
        List<MwTangibleassetsTable> assetsTable = mwAssetsManager.getAssetsTable(qparam);
        if(assetsTable.size()==0){
            return null;
        }
        List<AssetOrgDto> list=new ArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        List<Future<AssetOrgDto>> futureList = new ArrayList<>();
        for(MwTangibleassetsTable mw:assetsTable){
            GetDataByCallable<AssetOrgDto> getDataByCallable=new GetDataByCallable<AssetOrgDto>() {
                @Override
                public AssetOrgDto call() throws Exception {
                    MwTangibleassetsDTO assetsAndOrgs = mwAssetsManager.getAssetsAndOrgs(mw.getId());
                    if(null!=assetsAndOrgs){
                        if(assetsAndOrgs.getDepartment().size()>0){
                            String orgName = assetsAndOrgs.getDepartment().get(0).getOrgName();
                            Integer orgId = assetsAndOrgs.getDepartment().get(0).getOrgId();
                            return AssetOrgDto.builder().assetName(mw.getAssetsName())
                                    .OrgName(orgName).assetsTypeName(mw.getAssetsTypeName()).orgId(orgId).
                                            assetsTypeId(mw.getAssetsTypeId()).build();
                        }
                    }
                    return null;
                }
            };
            if(null!=getDataByCallable){
                Future<AssetOrgDto> f = executorService.submit(getDataByCallable);
                futureList.add(f);
            }
        }
        for (Future<AssetOrgDto> item : futureList) {
            try {
                AssetOrgDto assetOrgDto = item.get(30, TimeUnit.SECONDS);
                list.add(assetOrgDto);
            } catch (Exception e) {
                item.cancel(true);
                executorService.shutdown();
            }
        }
        executorService.shutdown();
        Map<Integer,Map<String,Integer>> mapResult=new HashMap<>();
        if(list.size()>0){
            list=list.stream().filter(f->f!=null).collect(Collectors.toList());
            //根据机构分组
            Map<Integer, List<AssetOrgDto>> collect = list.stream().collect(Collectors.groupingBy(AssetOrgDto::getOrgId));
            if(collect.size()>0){
                for (Map.Entry<Integer, List<AssetOrgDto>> en : collect.entrySet()) {
                    Integer orgId = en.getKey();
                    List<AssetOrgDto> dtos = en.getValue();
                    //机构下根据资产类型分组
                    Map<Integer, List<AssetOrgDto>> collect1 = dtos.stream().collect(Collectors.groupingBy(AssetOrgDto::getAssetsTypeId));
                    Map<String,Integer> map1=new HashMap<>();
                    for (Map.Entry<Integer, List<AssetOrgDto>> entry : collect1.entrySet()) {
                        String assetType = entry.getValue().get(0).getAssetsTypeName();
                        if(assetType==null){
                            assetType="unknown";
                        }
                        int size = entry.getValue().size();
                        map1.put(assetType,size);
                    }
                    mapResult.put(orgId,map1);
                }
            }
        }

        return mapResult;

    }

    public List<Map<String,List<Integer>>> getAsseOrg(QueryTangAssetsParam qparam){
        List<Map<String,List<Integer>>> resultList=new ArrayList<>();
        List<MwTangibleassetsTable> assetsTable = mwAssetsManager.getAssetsTable(qparam);
        if(assetsTable.size()==0){
            return resultList;
        }
        log.info("监控大屏资产概况资产数量"+assetsTable.size());
        List<String> ids = new ArrayList<>();//资产ID集合
        assetsTable.forEach(value->{
            ids.add(value.getId());
        });
        Map criteria = new HashMap();
        criteria.put("ids", ids);
        if(modelAssetEnable){
            criteria.put("moduleType", DataType.INSTANCE_MANAGE);
        }else{
            criteria.put("moduleType", DataType.ASSETS);
        }
        Reply reply = orgService.selectOrgMapByParamsAndIds(criteria);
        Map<String ,List<OrgMapperDTO>> orgMap = null;
        if(null != reply && PaasConstant.RES_SUCCESS == reply.getRes()){
            orgMap = (Map)reply.getData();
        }
        log.info("监控大屏资产概况机构对应资产"+orgMap);
        //调用接口查询资产机构数据
        List<AssetOrgDto> list=new ArrayList<>();
        for(MwTangibleassetsTable mw:assetsTable){
            List<OrgMapperDTO> orgMapperDTOS = orgMap.get(mw.getId());
            if(CollectionUtils.isEmpty(orgMapperDTOS))continue;
            for (OrgMapperDTO orgMapperDTO : orgMapperDTOS) {
                list.add(AssetOrgDto.builder().assetId(mw.getAssetsId()).assetName(mw.getAssetsName())
                        .itemAssetStatus(mw.getItemAssetsStatus()).OrgName(orgMapperDTO.getOrgName()).build());
            }
        }
        if(list.size()>0){
            log.info("监控大屏资产概况资产结果"+list);
            list=list.stream().filter(f->f!=null).collect(Collectors.toList());
            log.info("监控大屏资产概况资产结果2"+list);
            Map<String, List<AssetOrgDto>> collect = list.stream().collect(Collectors.groupingBy(AssetOrgDto::getOrgName));
            if(collect.size()>0){
                for(List<AssetOrgDto> a:collect.values()){
                    String orgName = a.get(0).getOrgName();
                    int normal = (int)a.stream().filter(s -> "NORMAL".equals(s.getItemAssetStatus())).count();
                    int total=a.size();
                    int abnormal=total-normal;
                    List<Integer> integers = Arrays.asList(total, normal, abnormal);
                    Map<String,List<Integer>> map=new HashMap<>();
                    map.put(orgName,integers);
                    resultList.add(map);
                }
            }
        }

        return resultList;
    }


    public MessageDto messageStatistics(FilterAssetsParam filterAssetsParam) {
//        List<String> hostIds = getAssetIdsByFilter(filterAssetsParam);
//        //解析数据失败hostid维-1
//        hostIds.add("-1");
//        if (hostIds.size() > 0) {

          //因告警信息表中修改了hostid字段，导致查询数据不正确，现改为直接查询，不通过hostid进行查询
          Integer todayCount = indexDao.getTodayMessage(null);
          Integer todaySuccessCount = indexDao.getTodaySuccessMessage(null);
          Integer sumCount = indexDao.getSumMessage(null);
          Integer sumSuccessCount = indexDao.getSumSuccessMessage(null);
          MessageCount todayMessageCount = MessageCount.builder().totalCount(todayCount == null?0:todayCount).successCount(todaySuccessCount == null?0:todaySuccessCount).failedCount(todayCount - todaySuccessCount).build();
          MessageCount sumMessageCount = MessageCount.builder().totalCount(sumCount== null?0:sumCount).successCount(sumSuccessCount== null?0:sumSuccessCount).failedCount(sumCount - sumSuccessCount).build();
          MessageDto messageDto = MessageDto.builder().todayMessage(todayMessageCount).sumMessage(sumMessageCount).build();
          return messageDto;

//        }
//        MessageCount todayMessageCount = MessageCount.builder().totalCount(0).successCount(0).failedCount(0).build();
//        MessageCount sumMessageCount = MessageCount.builder().totalCount(0).successCount(0).failedCount(0).build();
//        MessageDto messageDto = MessageDto.builder().todayMessage(todayMessageCount).sumMessage(sumMessageCount).build();
//        return messageDto;
    }

    //获得所有的资产统计
    public List<AlarmTypeCount> hostgroupListGetByName(String type, Integer userId) {
        Integer typeId = Integer.valueOf(type);
        List<AlarmTypeCount> alarmTypeCounts = new ArrayList<>();

        MwCommonAssetsDto mwCommonAssetsDto = MwCommonAssetsDto.builder().userId(userId).assetsTypeId(typeId).build();
        Map<String, Object> assets = mwAssetsManager.getAssetsByUserId(mwCommonAssetsDto);
        List<String> hostIds = new ArrayList<>();
        if (null != assets) {
            Object assetIds = assets.get("assetIds");
            if (null != assetIds) {
                hostIds = (List<String>) assetIds;
            }
        }
        if (hostIds.size() > 0) {
            MWZabbixAPIResult triggerList = mwtpServerAPI.problemget(1, hostIds);
            JsonNode trigger_data = (JsonNode) triggerList.getData();
            // AtomicInteger[] count = new AtomicInteger[5];
            Integer[] count = new Integer[]{0, 0, 0, 0, 0};
            if (trigger_data.size() > 0) {
                trigger_data.forEach(trigger -> {
                    String priority = trigger.get("severity").asText();
                    if (StringUtils.isNotEmpty(priority)) {
                        int priorityIndex = Integer.parseInt(priority);
                        count[priorityIndex - 1]++;
//                        if (null != priority) {
//                            switch (priority) {
//                                case "1":
//                                    count[0]++;
//                                    break;
//                                case "2":
//                                    count[1]++;
//                                    break;
//                                case "3":
//                                    count[2]++;
//                                    break;
//                                case "4":
//                                    count[3]++;
//                                    break;
//                                case "5":
//                                    count[4]++;
//                                    break;
//                                default:
//                                    break;
//                            }
//                        }
                    }
                });
            }
            for (int i = 1; i <= 5; i++) {
                AlarmTypeCount alarmTypeCount = new AlarmTypeCount();
                alarmTypeCount.setValue(count[i - 1]);
                alarmTypeCount.setName(SeverityUtils.SEVERITY.get(String.valueOf(i)));
                alarmTypeCounts.add(alarmTypeCount);
            }
        } else {
            for (int i = 1; i <= 5; i++) {
                AlarmTypeCount alarmTypeCount = new AlarmTypeCount();
                alarmTypeCount.setValue(0);
                alarmTypeCount.setName(SeverityUtils.SEVERITY.get(String.valueOf(i)));
                alarmTypeCounts.add(alarmTypeCount);
            }
        }
        return alarmTypeCounts;
    }

    public List<String> getAlertData(AlertParam dto){
        List<String> alertList=new ArrayList<>();
        MWZabbixAPIResult alert = mwtpServerAPI.alertGetByCurrent(dto.getMonitorServerId(), dto);
        if (alert.getCode() == 0) {
            JsonNode triggerid_data = (JsonNode) alert.getData();
            log.info("triggerid_data{}", triggerid_data);
            ArrayList triggeridList = new ArrayList<>();
            if (triggerid_data.size() > 0) {
                try {
                    triggerid_data.forEach(trigger -> {
                        JsonNode lastEvent = trigger.get("lastEvent");
                        if (lastEvent.size() > 0) {
                            triggeridList.add(trigger.get("lastEvent").get("eventid").asText());
                        }
                    });
                } catch (Exception e) {
                    log.info("triggeridList Error:" + e.getMessage().toString());
                }

            }
            if (triggeridList.size() > 0) {
                MWZabbixAPIResult currentAlter = mwtpServerAPI.eventGettByTriggers2(dto.getMonitorServerId(), triggeridList);
                if(currentAlter.getCode()==0){
                    JsonNode event_data = (JsonNode) currentAlter.getData();
                    if (null != event_data && event_data.size() > 0) {
                        event_data.forEach(event->{
                            if (null != event.get("hosts") && event.get("hosts").size() > 0) {
                                String acknowledged = event.get("acknowledged").asText();
                                alertList.add(acknowledged);
                            }
                        });
                    }
                }
            }

        }
        return alertList;
    }
    //告警统计
    public JSONObject getAlertCount(FilterAssetsParam filterAssetsParam) {
        JSONObject jsObject = new JSONObject();
        if(debug){
            log.info("/getAlertCountAcknowledged/browse:getAssetIdsByServerId start"+new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS").format(new Date()));
        }
        Map<Integer, List<String>> map = getAssetIdsByServerId(filterAssetsParam);
        if(debug){
            log.info("/getAlertCountAcknowledged/browse:getAssetIdsByServerId end"+new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS").format(new Date()));
        }
        if (null != map && map.size() > 0) {
            if(debug){
                log.info("/getAlertCountAcknowledged/browse:mwtpServerAPI.problemget start"+new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS").format(new Date()));
            }
            int ccount=0;
            int icount=0;
            for (Integer key : map.keySet()) {
                List<String> hostIds = map.get(key);
                if (hostIds.size() > 0) {
                    AlertParam alertParam=new AlertParam();
                    alertParam.setMonitorServerId(key);
                    alertParam.setHostids(hostIds);
                    List<String> alertData = getAlertData(alertParam);
                    for (int i = 0; i <alertData.size() ; i++) {
                        if("0".equals(alertData.get(i))){
                            ccount++;
                        }else {
                            icount++;
                        }
                    }
                }
//                List<String> hostIds = map.get(key);
//                if (hostIds.size() > 0) {
//                    MWZabbixAPIResult ackTrue = mwtpServerAPI.problemget(key, hostIds, true);
//                    MWZabbixAPIResult ackFalse = mwtpServerAPI.problemget(key, hostIds, false);
//                    if (ackTrue.getCode() == 0 && ackFalse.getCode() == 0) {
//                        JsonNode ackTrueList = (JsonNode) ackTrue.getData();
//                        JsonNode ackFalseList = (JsonNode) ackFalse.getData();
//                        Object insert = jsObject.get("insertCount");
//                        Object close = jsObject.get("closeCount");
//                        if (null != insert) {
//                            jsObject.put("insertCount", ackTrueList.size() + (int) insert);//已确认告警
//                        } else {
//                            jsObject.put("insertCount", ackTrueList.size());
//
//                        }
//                        if (null != close) {
//                            jsObject.put("closeCount", ackFalseList.size() + (int) close);//未确认告警
//                        } else {
//                            jsObject.put("closeCount",  ackFalseList.size());
//                        }
//                    } else {
//                        jsObject.put("insertCount", 0);
//                        jsObject.put("closeCount", 0);
//                    }
//                }
            }
            jsObject.put("insertCount", icount);
            jsObject.put("closeCount", ccount);
            if(debug){
                log.info("/getAlertCountAcknowledged/browse:mwtpServerAPI.problemget end"+new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS").format(new Date()));
            }
        }

        if (filterAssetsParam.getType().equals(DataType.SCREEN.getName())) {
            if(debug){
                log.info("/getAlertCountAcknowledged/browse:messageStatistics start"+new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS").format(new Date()));
            }
            MessageDto messageDto = messageStatistics(filterAssetsParam);
            jsObject.put("messageDto", messageDto);
            if(debug){
                log.info("/getAlertCountAcknowledged/browse:messageStatistics end"+new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS").format(new Date()));
            }
        }
        return jsObject;
    }

    /**
     * 获得全部的hostIds
     *
     * @param param
     * @return
     */
    private List<String> getAssetIdsByFilter(FilterAssetsParam param) {
        MwCommonAssetsDto mwCommonAssetsDto = dao.getFilterAssets(param);
        if (null == mwCommonAssetsDto) {
            mwCommonAssetsDto = MwCommonAssetsDto.builder().userId(param.getUserId()).build();
        }
        Map<String, Object> assets = mwAssetsManager.getAssetsByUserId(mwCommonAssetsDto);

        if(StringUtils.isNotBlank(mwCommonAssetsDto.getFilterOrgId())&&assets!=null){
            assets= getAssetByOrgId(assets,mwCommonAssetsDto.getFilterOrgId());
        }

        List<String> hostIds = new ArrayList<>();
        if (null != assets) {
            Object assetIds = assets.get("assetIds");
            if (null != assetIds) {
                hostIds = (List<String>) assetIds;
            }
        }
        return hostIds;
    }

    /**
     * 根据serverId分组hostids
     *
     * @param param
     * @return
     */
    private Map<Integer, List<String>> getAssetIdsByServerId(FilterAssetsParam param) {
        MwCommonAssetsDto mwCommonAssetsDto = dao.getFilterAssets(param);
        log.info("资产过滤条件："+mwCommonAssetsDto);
        if (null == mwCommonAssetsDto) {
            mwCommonAssetsDto = MwCommonAssetsDto.builder().userId(param.getUserId()).assetsTypeId(param.getAssetsTypeId()).build();
        }
        Map<String, Object> assets = mwAssetsManager.getAssetsByUserId(mwCommonAssetsDto);
        if(StringUtils.isNotBlank(mwCommonAssetsDto.getFilterOrgId())&&assets!=null){
            assets= getAssetByOrgId(assets,mwCommonAssetsDto.getFilterOrgId());
        }
        log.info("首页assets：" + assets);
        Map<Integer, List<String>> map = mwAssetsManager.getAssetsByServerId(assets);
        return map;
    }


    /**
     * 获取当前登录用户所能看到的资产
     */
    private List<String> getCurrLoginUserAssets(){
        //获取当前登录用户信息
        GlobalUserInfo globalUser = userService.getGlobalUser();
        //根据当前登录用户查询属于该用户权限的资产ID集合
        return userService.getAllTypeIdList(globalUser, DataType.ASSETS);
    }

    public Map<String,Object> getAssetByOrgId(Map<String,Object> assets,String assetOrgId){
        List<MwTangibleassetsTable> mwTangibleassetsDTOS;
        List<String> assetIds =new ArrayList<>();
        List<String> ids =new ArrayList<>();
        List<MwTangibleassetsTable> result=new ArrayList<>();
        List<List<Integer>> filterOrgIds = JSON.parseObject(assetOrgId, List.class);
        if(assets.get("assetsList")!=null){
            mwTangibleassetsDTOS= (List<MwTangibleassetsTable>) assets.get("assetsList");
            if(mwTangibleassetsDTOS.size()>0){
                ExecutorService executorService = Executors.newFixedThreadPool(20);
                List<Future<MwTangibleassetsTable>> futureList = new ArrayList<>();
                //获取当前登录用户的资产，如果当前登录用户没有该资产，过滤掉
                List<String> currLoginUserAssets = getCurrLoginUserAssets();
                for(MwTangibleassetsTable mw:mwTangibleassetsDTOS){
                    if(CollectionUtils.isNotEmpty(currLoginUserAssets) && !currLoginUserAssets.contains(mw.getId()))continue;
                    GetDataByCallable<MwTangibleassetsTable> getDataByCallable=new GetDataByCallable<MwTangibleassetsTable>() {
                        @Override
                        public MwTangibleassetsTable call() throws Exception {
                            //根据资产id获取对应机构，一个资产至少包含一个机构
                            MwTangibleassetsDTO assetsAndOrgs = mwAssetsManager.getAssetsAndOrgs(mw.getId());
                            if(null!=assetsAndOrgs){
                                if(assetsAndOrgs.getDepartment().size()>0){
                                    List<OrgDTO> department = assetsAndOrgs.getDepartment();
                                    //一个资产多个机构
                                    List<Integer> ss=new ArrayList<>();
                                    for (OrgDTO o : department) {
                                        if(null!=o){
                                            ss.add(o.getOrgId());
                                        }
                                    }
                                    if(ss.size()>0){
                                        for (List<Integer> filterOrgId : filterOrgIds) {
                                            if(ss.contains(filterOrgId.get(filterOrgId.size()-1))){
                                                return mw;
                                            }
                                        }
                                        return null;
                                    }
                                }
                            }
                            return null;
                        }
                    };
                    if(null!=getDataByCallable){
                        Future<MwTangibleassetsTable> f = executorService.submit(getDataByCallable);
                        futureList.add(f);
                    }
                }

                for (Future<MwTangibleassetsTable> item : futureList) {
                    try {
                        MwTangibleassetsTable mt = item.get(30, TimeUnit.SECONDS);
                        if(mt!=null){
                            result.add(mt);
                        }
                    } catch (Exception e) {
                        item.cancel(true);
                        executorService.shutdown();
                    }
                }
                executorService.shutdown();
            }
            if(result.size()>0){
                result.forEach(s->assetIds.add(s.getAssetsId()));
                result.forEach(s->ids.add(s.getId()));
            }
        }
        mwTangibleassetsDTOS=result;
        Map<String, Object> map = new HashMap<>();
        map.put("assetsList", mwTangibleassetsDTOS);
        map.put("assetIds", assetIds);
        map.put("ids", ids);
        return map;
    }


    //活动告警列表
    public List<HistEventDto> getAlertEvent(FilterAssetsParam filterAssetsParam) {
        List<HistEventDto> list = new ArrayList<>();
        Map<Integer, List<String>> map = getAssetIdsByServerId(filterAssetsParam);
        if (null != map && map.size() > 0) {
            for (Integer key : map.keySet()) {
                List<String> hostIds = map.get(key);
                if (hostIds.size() > 0) {
                    AlertParam dto = new AlertParam();
                    dto.setHostids(hostIds);
                    dto.setMonitorServerId(key);
                    List<ZbxAlertDto> currentAltertList = alertManager.getCurrentAltertList(dto);
                    if(currentAltertList.size()>0){
                        for (ZbxAlertDto mwAlertDto : currentAltertList) {
                            if(mwAlertDto!=null){
                                HistEventDto alertdto = new HistEventDto();
                                alertdto.setSeverity(mwAlertDto.getSeverity());
                                alertdto.setName(mwAlertDto.getName());
                                alertdto.setTime(mwAlertDto.getClock());
                                alertdto.setIp(mwAlertDto.getIp());
                                alertdto.setId(mwAlertDto.getAssetsId());
                                alertdto.setAssetsName(mwAlertDto.getObjectName());
                                list.add(alertdto);
                            }
                        }
                    }
                }
            }
        }
        return list;
    }

    //过去N条事件数据默认显示25条
    public List<HistEventDto> getHistEvent(Integer count, FilterAssetsParam filterAssetsParam) {
        List<HistEventDto> list = new ArrayList<>();
        Map<Integer, List<String>> map = getAssetIdsByServerId(filterAssetsParam);
        if (null != map && map.size() > 0) {
            for (Integer key : map.keySet()) {
                List<String> hostIds = map.get(key);
                MWZabbixAPIResult histEvent = mwtpServerAPI.getHistEvent(key, count, hostIds);
                if (histEvent.getCode() == 0) {
                    List<HistEventDto> data = getData(histEvent);
                    for (HistEventDto histEventDto : data) {
                        list.add(histEventDto);
                    }
                } else {
                    log.error("getHistEvent histEvent.getData().toString(){}", histEvent.getData().toString());
                }
            }
        }
        return list;
    }

    // 排行榜 根据所有启用状态的主机，来查询它们的使用率排行榜
    public ItemRank getHostRank(String name, FilterAssetsParam filterAssetsParam) {
        ItemRank itemRank = new ItemRank();
        if(debug){
            log.info("/hostUserRatioList/browse:getAssetIdsByServerId start"+new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS").format(new Date()));
        }
        Map<Integer, List<String>> map = getAssetIdsByServerId(filterAssetsParam);
        if(debug){
            log.info("/hostUserRatioList/browse:getAssetIdsByServerId end"+new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS").format(new Date()));
        }
        if (null != map && map.size() > 0) {

            if(debug){
                log.info("/hostUserRatioList/browse:mwtpServerAPI.itemGetbyType start"+new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS").format(new Date()));
            }
            for (Integer key : map.keySet()) {
                List<String> hostIds = map.get(key);
                MWZabbixAPIResult result = mwtpServerAPI.itemGetbyType(key, name, hostIds);
                
                if(debug) {
                    log.info("result.code:" + result.code + "result.key:" + key + "result.name:" + name + "result.hostIds:" + hostIds + "date:" + result.getData());
                }
                if (result.code == 0) {
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
                                                itemNameRank.setUnits(dataUnits);
                                            }
//                                if (null != map1) {
//                                    itemNameRank.setSortlastValue(Double.valueOf(lastvalue));
//                                    itemNameRank.setLastValue(Double.parseDouble(map1.get("value")));
//                                    itemNameRank.setUnits(map1.get("units"));
//                                }
                                        if (name.equals("DISK_UTILIZATION") ) {
                                            int i = item.get("name").asText().indexOf("]");
                                            if (i != -1) {
                                                itemNameRank.setType(item.get("name").asText().substring(1, i));
                                                itemNameRank.setName(assets.getAssetsName() + item.get("name").asText().substring(0, i + 1));
                                            }
                                        }
                                        if (name.equals("INTERFACE_IN_TRAFFIC") || name.equals("INTERFACE_OUT_TRAFFIC") || name.equals("INTERFACE_IN_UTILIZATION") || name.equals("INTERFACE_OUT_UTILIZATION")) {
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
                        //一个资产有多个cpu取最大一个
                        if("CPU_UTILIZATION".equals(name)){
                            Map<String, List<ItemNameRank>> collect = itemNameRanks.stream().collect(Collectors.groupingBy(ItemNameRank::getAssetsId));
                            ItemNameRank ite=new ItemNameRank();
                            List<ItemNameRank> list=new ArrayList<>();
                            for (List<ItemNameRank> ranks : collect.values()) {
                                //取平均值
                                double value = 0;
                                if(CollectionUtils.isEmpty(ranks))continue;
                                for (ItemNameRank rank : ranks) {
                                    value += rank.getLastValue();
                                }
                                ite = ranks.get(0);
                                ite.setLastValue(new BigDecimal(value/ranks.size()).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue());
                                list.add(ite);
                            }
                            itemNameRanks=list;
                        }
                        if(itemNameRanks != null){
                            if(CollectionUtils.isNotEmpty(itemNameRanks)){
                                Collections.sort(itemNameRanks, new Comparator<ItemNameRank>() {
                                    @Override
                                    public int compare(ItemNameRank o1, ItemNameRank o2) {
                                        return o2.getLastValue().compareTo(o1.getLastValue());
                                    }
                                });
                            }
                        }
                        if("INTERFACE_IN_TRAFFIC".equals(name) || "INTERFACE_OUT_TRAFFIC".equals(name)){
                            log.info("首页流量接收2长度为："+itemNameRanks.size());
                            //判断但是否是流量，如果是流量，需要先排序
                            for (ItemNameRank rank : itemNameRanks) {
                                Double lastValue = rank.getLastValue();
                                String units = rank.getUnits();
                                Map<String, String> valueMap = UnitsUtil.getValueMap(lastValue + "", "Kbps", units);
                                String value = valueMap.get("value");
                                rank.setLinkSortValue(Double.parseDouble(value));
                            }
                            //数据排序
                            if(CollectionUtils.isNotEmpty(itemNameRanks)){
                                Collections.sort(itemNameRanks, new Comparator<ItemNameRank>() {
                                    @Override
                                    public int compare(ItemNameRank o1, ItemNameRank o2) {
                                        return o2.getLinkSortValue().compareTo(o1.getLinkSortValue());
                                    }
                                });
                            }
                        }
                        if (filterAssetsParam.getType().equals(DataType.SCREEN.getName())) {
                            if (itemNameRanks.size() > 10) {
                                itemNameRanks = itemNameRanks.subList(0, 10);
                            }
                        } else {
                            if (itemNameRanks.size() > 20) {
                                itemNameRanks = itemNameRanks.subList(0, 20);
                            }
                        }
                        List<String> node = new ArrayList<>();
                        node.add("资产名称");
//                        node.add("IP地址");
                        switch (name) {
                            case "CPU_UTILIZATION":
                                node.add("CPU利用率");
                                TitleRank titleRank3 = TitleRank.builder().name("CPU利用率").fieldName("lastValue").build();
                                titleRanks.add(titleRank3);
                                break;
                            case "DISK_UTILIZATION":
                                node.add("磁盘分区名称");
                                node.add("磁盘利用率");
                                TitleRank titleRank4 = TitleRank.builder().name("磁盘分区名称").fieldName("type").build();
                                TitleRank titleRank5 = TitleRank.builder().name("磁盘利用率").fieldName("lastValue").build();
                                titleRanks.add(titleRank4);
                                titleRanks.add(titleRank5);
                                break;
                            case "MEMORY_UTILIZATION":
                                node.add("内存利用率");
                                TitleRank titleRank6 = TitleRank.builder().name("内存利用率").fieldName("lastValue").build();
                                titleRanks.add(titleRank6);
                                break;
                            case "ICMP_LOSS":
                                node.add("节点丢包率");
                                TitleRank titleRank7 = TitleRank.builder().name("节点丢包率").fieldName("lastValue").build();
                                titleRanks.add(titleRank7);
                                break;
                            case "INTERFACE_IN_TRAFFIC":
                                node.add("接口名称");
                                node.add("每秒接收流量");
                                TitleRank titleRank8 = TitleRank.builder().name("接口名称").fieldName("type").build();
                                TitleRank titleRank9 = TitleRank.builder().name("每秒接收流量").fieldName("lastValue").build();
                                titleRanks.add(titleRank8);
                                titleRanks.add(titleRank9);
                                break;
                            case "INTERFACE_OUT_TRAFFIC":
                                node.add("接口名称");
                                node.add("每秒发送流量");
                                TitleRank titleRank10 = TitleRank.builder().name("接口名称").fieldName("type").build();
                                TitleRank titleRank11 = TitleRank.builder().name("每秒发送流量").fieldName("lastValue").build();
                                titleRanks.add(titleRank10);
                                titleRanks.add(titleRank11);
                                break;
                            case "ICMP_RESPONSE_TIME":
                                node.add("节点延时");
                                TitleRank titleRank12 = TitleRank.builder().name("节点延时").fieldName("lastValue").build();
                                titleRanks.add(titleRank12);
                                break;
                            default:
                                break;
                        }
                        itemRank.setTitleNode(node);
                        itemRank.setItemNameRankList(itemNameRanks);
                        itemRank.setTitleRanks(titleRanks);
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
            if(debug){
                log.info("/hostUserRatioList/browse:mwtpServerAPI.itemGetbyType end"+new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS").format(new Date()));
            }
        }
        return itemRank;
    }

    //查询当前用户所能监控的网站的下载速度和时间排行榜
    public ItemRank getItemWebRank(FilterAssetsParam filterAssetsParam, String type) throws Exception{
        ItemRank itemRank = new ItemRank();
        List<TitleRank> titleRanks = new ArrayList<>();
        Map<Integer, List<String>> maps = getAssetIdsByServerId(filterAssetsParam);
        if (null != maps && maps.size() > 0) {
            List<ItemNameRank> list = new ArrayList<>();
            for (Integer key : maps.keySet()) {
                List<String> hostIds = maps.get(key);
                MWZabbixAPIResult result = mwtpServerAPI.HttpTestGet(key, hostIds);
                TitleRank titleRank1 = TitleRank.builder().name("资产名称").fieldName("name").build();
                TitleRank titleRank2 = TitleRank.builder().name("IP地址").fieldName("ip").build();
                titleRanks.add(titleRank1);
                titleRanks.add(titleRank2);

                if (result.getCode() == 0) {
                    JsonNode data = (JsonNode) result.getData();
                    if (data.size() > 0) {
                        for (JsonNode httpTest : data) {
                            ItemNameRank itemWebRank = new ItemNameRank();
                            itemWebRank.setName(httpTest.get("name").asText());
                            String stepsname = httpTest.get("steps").get(0).get("name").asText();
                            String hostid = httpTest.get("hostid").asText();
                            //查询zabbix的数据库 未解决
                            Map<String, String> map = mwWebZabbixManger.getItemId(key, hostid, stepsname);
                            if (map.size() > 0) {
                                String lastvalue = null;
                                String units = "";
                                if (type.equals("in")) {
                                    lastvalue = map.get("web.test.in[" + stepsname + "," + stepsname + "," + "bps]");
                                    units = "bps";
                                } else if (type.equals("time")) {
                                    lastvalue = map.get("web.test.time[" + stepsname + "," + stepsname + "," + "resp]");
                                    units = "ms";
                                }

                                if ( lastvalue != null) {
                                        itemWebRank.setSortlastValue(Double.parseDouble(lastvalue));
                                        Map<String, String> map1 = UnitsUtil.getValueAndUnits(lastvalue, units);
                                        if (null != map1) {
                                            itemWebRank.setLastValue(Double.parseDouble(map1.get("value")));
                                            itemWebRank.setUnits(map1.get("units"));
                                        }
                                }
                                AssetsDto assetsNameAndIPById = mwModelViewCommonService.getAssetsById(hostid ,key);
                                //前端页面跳转用到随便取固定名称
                                itemWebRank.setIsWebMonitor(true);
                                itemWebRank.setIp(assetsNameAndIPById.getAssetsIp());
                                list.add(itemWebRank);
                            }
                        }
                    }
                }
            }
            if (list.size() > 0) {
                Collections.sort(list, new ItemNameRank());
                if (list.size() > 10) {
                    list.subList(0, 10);
                }
            }

            itemRank.setItemNameRankList(list);
            itemRank.setTitleRanks(titleRanks);
            List<String> node = new ArrayList<>();
            node.add("资产名称");
            node.add("IP地址");
            if (type.equals("in")) {
                TitleRank titleRank3 = TitleRank.builder().name("网站的下载速度").fieldName("lastValue").build();
                node.add("网站的下载速度");
                titleRanks.add(titleRank3);
            } else if (type.equals("time")) {
                TitleRank titleRank3 = TitleRank.builder().name("网站的响应时间").fieldName("lastValue").build();
                node.add("网站的响应时间");
                titleRanks.add(titleRank3);
            }
            itemRank.setTitleNode(node);
        }
        return itemRank;

    }


    private List<HistEventDto> getData(MWZabbixAPIResult histEvent) {
        List<HistEventDto> list = new ArrayList<>();
        if (histEvent.getCode() == 0) {
            JsonNode histEventData = (JsonNode) histEvent.getData();
            if (histEventData.size() > 0) {
                histEventData.forEach(enent -> {
                    HistEventDto dto = new HistEventDto();
                    dto.setSeverity(MWUtils.SEVERITY.get(enent.get("severity").asText()));
                    dto.setName(enent.get("name").asText());
                    long clock = enent.get("clock").asLong();
                    String time = SeverityUtils.CalculateTime(SeverityUtils.getDate(clock));
                    dto.setTime(time);
                    list.add(dto);
                });
            }
        } else {
            log.error("getData histEvent.getData().toString(){}", histEvent.getData().toString());

        }
        return list;
    }


    private String getDataValue(MWZabbixAPIResult result) {
        JsonNode node = (JsonNode) result.getData();
        String value = null;
        if (node.size() > 0) {
            value = node.get(0).get("value").asText();
        }
        return value;
    }


    public List<AlertPriorityType> getAssetsCountByType(Integer userId, Integer moduleId) {
        List<AlertPriorityType> list = new ArrayList<>();
        List<Integer> typeIds = new ArrayList<>();
        typeIds.add(MWUtils.ASSETS_TYPE.get("服务器"));//服务器
        typeIds.add(MWUtils.ASSETS_TYPE.get("网络设备"));//网络设备
        typeIds.add(MWUtils.ASSETS_TYPE.get("应用"));//应用
        typeIds.add(MWUtils.ASSETS_TYPE.get("中间件"));//中间件

        for (Integer assetsTypeId : typeIds) {
            AlertPriorityType alertPriorityType = new AlertPriorityType();
            alertPriorityType.setGroupName(SeverityUtils.TYPE2ZHN.get(assetsTypeId));
            FilterAssetsParam filterAssetsParam = FilterAssetsParam.builder().modelId(moduleId).userId(userId).type(DataType.INDEX.getName()).build();
            MwCommonAssetsDto mwCommonAssetsDto = dao.getFilterAssets(filterAssetsParam);
            if (null == mwCommonAssetsDto) {
                mwCommonAssetsDto = MwCommonAssetsDto.builder().userId(userId).assetsTypeId(assetsTypeId).build();
            }
            Map<String, Object> assets = mwAssetsManager.getAssetsByUserId(mwCommonAssetsDto);
            List<String> hostIds = new ArrayList<>();
            if (null != assets) {
                Object assetIds = assets.get("assetIds");
                if (null != assetIds) {
                    hostIds = (List<String>) assetIds;
                }
            }
            alertPriorityType.setSum(hostIds.size());
            list.add(alertPriorityType);
        }
        return list;
    }

    public AlertTodayHistory getAlertTodayHistory(Integer userId, Integer moduleId) {
        List<String> dates = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        HashMap<Integer, Long> countMap = new HashMap<>();
        AlertTodayHistory alertTodayHistory = new AlertTodayHistory();
        FilterAssetsParam filterAssetsParam = FilterAssetsParam.builder().modelId(moduleId).userId(userId).type(DataType.INDEX.getName()).build();
        MwCommonAssetsDto mwCommonAssetsDto = dao.getFilterAssets(filterAssetsParam);
        if (null == mwCommonAssetsDto) {
            mwCommonAssetsDto = MwCommonAssetsDto.builder().userId(userId).assetsTypeId(0).build();
        }
        Map<String, Object> assets = mwAssetsManager.getAssetsByUserId(mwCommonAssetsDto);
        Map<Integer, List<String>> maps = mwAssetsManager.getAssetsByServerId(assets);

        for (Integer key : maps.keySet()) {
            List<String> hostIds = maps.get(key);
            if (hostIds.size() > 0) {
                Calendar calendar = Calendar.getInstance();
                for (int i = 6; i > 0; i--) {
                    if (i < 6) {
                        calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) - 1);
                    }
                    Integer count = alertManager.getAlertTodayHistory(key, hostIds, String.valueOf(calendar.getTimeInMillis()).substring(0, 10));
                    if (null != count) {
                        if (countMap.containsKey(i)) {
                            countMap.put(i, Long.valueOf(count) + countMap.get(i));
                        } else {
                            countMap.put(i, Long.valueOf(count));
                            dates.add(calendar.getTime().toString().substring(11, 19));
                        }
                    }
                }
            }
        }
        if (countMap.size() > 0) {
            for (Integer i : countMap.keySet()) {
                counts.add(countMap.get(i));

            }
        }
        Collections.reverse(counts);
        Collections.reverse(dates);
        alertTodayHistory.setCount(counts);
        alertTodayHistory.setDate(dates);
        return alertTodayHistory;
    }

    @Autowired
    private StringRedisTemplate redisTemplate;

    private String genRedisKey(String methodName, String objectName, Integer uid) {
        StringBuffer sb = new StringBuffer();
        sb.append(methodName).append(":").append(objectName)
                .append("_").append(uid);
        return sb.toString();
    }

    private void saveToRedis(String key, String value) {
        if (redisTemplate.hasKey(key)) {
            redisTemplate.delete(key);
        }
        redisTemplate.opsForValue().set(key, value, 60 * 10, TimeUnit.SECONDS);
    }


    public String getTypeListAsync() {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        SearchRequest searchRequest = new SearchRequest();
        HashSet set = new HashSet();
        try {
            GetAliasesResponse alias = restHighLevelClient.indices().getAlias(new GetAliasesRequest(), RequestOptions.DEFAULT);
            List<String> list = new ArrayList<>();
            Map<String, Set<AliasMetadata>> map = alias.getAliases();
            map.forEach((k, v) -> {
                if (!k.startsWith(".")) {
                    list.add(k);
                }
            });
            for (String index : list) {
                searchRequest.indices(index);
                ActionListener<SearchResponse> listener = new ActionListener<SearchResponse>() {
                    @Override
                    public void onResponse(SearchResponse searchResponse) {
                        for (SearchHit searchHit : searchResponse.getHits().getHits()) {
                            Object type = searchHit.getSourceAsMap().get("type");
                            if (null != type) {
                                set.add(type.toString());
                            }
                        }
                        redisTemplate.opsForValue().set(MWUtils.REDIS_SECURITY_TYPE, JSON.toJSONString(set), 1, TimeUnit.DAYS);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        log.error("(getTypeListAsync)时间格式转换异常|IO异常", e);
                    }
                };
                restHighLevelClient.searchAsync(searchRequest, RequestOptions.DEFAULT, listener);

            }
        } catch (IOException e) {
            log.error("(getTypeListAsync)IO异常{}", e);
            return JSON.toJSONString(set);
        }
        return JSON.toJSONString(set);
    }


    public Object getSecurityCountByField(String fieldName) {
        try {
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            if (fieldName.equals("@timestamp")) {
                Map<String, Object> map = new HashMap<>();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ");
                String lte = sdf.format(new Date());
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                String gte = sdf.format(calendar.getTime());
                SearchSourceBuilder query = searchSourceBuilder.query(QueryBuilders.boolQuery()
                        .filter(QueryBuilders.rangeQuery("@timestamp").from(MWUtils.getUtcTime(gte)).to(MWUtils.getUtcTime(lte))));//56558536  145416669 @timestamp
                CountRequest countRequest = new CountRequest().source(query);
                CountResponse count = restHighLevelClient.count(countRequest, RequestOptions.DEFAULT);
                Map<String, String> mapLeft = UnitsUtil.getValueAndUnits(String.valueOf(count.getCount()), NewUnits.T.getUnits());
                map.put("nameLeft", "今日统计");
                map.put("valueLeft", mapLeft.get("value") + mapLeft.get("units"));
                CountRequest countRequestHist = new CountRequest().source(searchSourceBuilder.query(QueryBuilders.matchAllQuery()));
                CountResponse countHist = restHighLevelClient.count(countRequestHist, RequestOptions.DEFAULT);
                Map<String, String> mapRight = UnitsUtil.getValueAndUnits(String.valueOf(countHist.getCount()), NewUnits.T.getUnits());
                map.put("nameRight", "总日志量");
                map.put("valueRight", mapRight.get("value") + mapRight.get("units"));
                return map;
            } else if (fieldName.equals("disposalStatus")) {
                Map<String, Object> map = new HashMap<>();
                SearchSourceBuilder query0 = searchSourceBuilder.query(QueryBuilders.boolQuery()
                        .must(QueryBuilders.matchQuery("disposalStatus", "0")));
                CountRequest countRequest = new CountRequest().source(query0);
                CountResponse count = restHighLevelClient.count(countRequest, RequestOptions.DEFAULT);
                Map<String, String> mapLeft = UnitsUtil.getValueAndUnits(String.valueOf(count.getCount()), NewUnits.T.getUnits());
                map.put("nameLeft", "未处理");
                map.put("valueLeft", mapLeft.get("value") + mapLeft.get("units"));
                CountRequest countRequest1 = new CountRequest().source(searchSourceBuilder.query(QueryBuilders.boolQuery()
                        .must(QueryBuilders.matchQuery("disposalStatus", "1"))));
                CountResponse countHist = restHighLevelClient.count(countRequest1, RequestOptions.DEFAULT);
                Map<String, String> mapRight = UnitsUtil.getValueAndUnits(String.valueOf(countHist.getCount()), NewUnits.T.getUnits());
                map.put("nameRight", "已处理");
                map.put("valueRight", mapRight.get("value") + mapLeft.get("units"));
                return map;
            } else if (fieldName.equals("alertLevel")) {
                List<SecurityDto> list = new ArrayList<>();
                for (int i = 1; i < 6; i++) {
                    SearchSourceBuilder query = searchSourceBuilder.query(QueryBuilders.boolQuery()
                            .must(QueryBuilders.matchQuery("alertLevel", String.valueOf(i))));
                    CountRequest countRequest = new CountRequest().source(query);
                    CountResponse count = restHighLevelClient.count(countRequest, RequestOptions.DEFAULT);
                    SecurityDto securityDto = SecurityDto.builder().name(String.valueOf(i)).value(count.getCount()).build();
                    list.add(securityDto);
                }
                return list;
            } else if (fieldName.equals("type")) {
                List<SecurityDto> list1 = new ArrayList<>();
                String redisValue = redisTemplate.opsForValue().get(MWUtils.REDIS_SECURITY_TYPE);
                if (null == redisValue) {
                    String typeList = getTypeList();
                    redisValue = typeList;
                }
                List<String> list = JSONArray.parseArray(redisValue, String.class);
                for (String type : list) {
//                    SearchSourceBuilder query = searchSourceBuilder.query(QueryBuilders.boolQuery()
//                            .must(QueryBuilders.matchAllQuery().queryName(type)));
                    CountRequest countRequest = new CountRequest().query(QueryBuilders.matchQuery("type", type));
                    CountResponse count = restHighLevelClient.count(countRequest, RequestOptions.DEFAULT);
                    SecurityDto securityDto = SecurityDto.builder().name(type).value(count.getCount()).build();
                    list1.add(securityDto);
                }
                return list1;
            }
        } catch (IOException | ParseException e) {
            log.error("(getSecurityCountByField)时间格式转换异常|IO异常{}", e);
            return null;
        }
        return null;
    }

    public Object getLogCount(FilterAssetsParam filterAssetsParam) {
        Map<String, Object> map = new HashMap<>();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ");
            String lte = sdf.format(new Date());
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            String gte = sdf.format(calendar.getTime());
            MwCommonAssetsDto mwCommonAssetsDto = dao.getFilterAssets(filterAssetsParam);
            if (null == mwCommonAssetsDto) {
                //  mwCommonAssetsDto = MwCommonAssetsDto.builder().userId(filterAssetsParam.getUserId()).monitorMode(11).build();
                mwCommonAssetsDto = MwCommonAssetsDto.builder().userId(filterAssetsParam.getUserId()).build();
            }
            List<String> hostIps = mwAssetsManager.getLogHostList(mwCommonAssetsDto);
            if (hostIps.size() > 0) {
                BoolQueryBuilder queryBuilder2 = QueryBuilders.boolQuery();
                for (String hostIp : hostIps) {
                    queryBuilder2 = queryBuilder2.should(QueryBuilders.termQuery("host.name", hostIp));
                    queryBuilder2 = queryBuilder2.should(QueryBuilders.termQuery("fields.host", hostIp));
                }
                BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery("@timestamp").from(MWUtils.getUtcTime(gte)).to(MWUtils.getUtcTime(lte)))
                        .must(queryBuilder2);//56558536  145416669 @timestamp
                CountRequest countRequest = new CountRequest().query(queryBuilder);
                CountResponse count = restHighLevelClient.count(countRequest, RequestOptions.DEFAULT);
                Map<String, String> mapLeft = UnitsUtil.getValueAndUnits(String.valueOf(count.getCount()), NewUnits.T.getUnits());
                map.put("valueLeft", mapLeft.get("value") + mapLeft.get("units"));
                map.put("nameLeft", "今日数量");
                CountRequest countRequestHist = new CountRequest().query(QueryBuilders.boolQuery().must(queryBuilder2));
                CountResponse countHist = restHighLevelClient.count(countRequestHist, RequestOptions.DEFAULT);
                Map<String, String> mapRight = UnitsUtil.getValueAndUnits(String.valueOf(countHist.getCount()), NewUnits.T.getUnits());
                map.put("valueRight", mapRight.get("value") + mapRight.get("units"));
                map.put("nameRight", "历史数量");
            }
        } catch (IOException | ParseException e) {
            log.error("(getSecurityCountByField)时间格式转换异常|IO异常{}", e);
            return map;
        }
        return map;

    }


    public String getTypeList() {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        SearchRequest searchRequest = new SearchRequest();

        SearchResponse search = null;
        HashSet set = new HashSet();
        try {
            GetAliasesResponse alias = restHighLevelClient.indices().getAlias(new GetAliasesRequest(), RequestOptions.DEFAULT);
            List<String> list = new ArrayList<>();
            Map<String, Set<AliasMetadata>> map = alias.getAliases();
            map.forEach((k, v) -> {
                if (!k.startsWith(".")) {
                    list.add(k);
                }
            });
            for (String index : list) {
                searchRequest.indices(index);
                search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
                for (SearchHit searchHit : search.getHits().getHits()) {
                    //set.add(searchHit.getType());
                    Object type = searchHit.getSourceAsMap().get("type");
                    if (null != type) {
                        set.add(type.toString());
                    }
                }
            }
        } catch (IOException e) {
            log.error("(saveSecurityType)IO异常{}", e.getMessage());
            return JSON.toJSONString(set);
        }
        redisTemplate.opsForValue().set(MWUtils.REDIS_SECURITY_TYPE, JSON.toJSONString(set), 1, TimeUnit.DAYS);
        return JSON.toJSONString(set);
    }

    /**
     * @param type sourceIp deviceIp
     * @return
     */
    public List<Map> getOccurCount(String type) {
        List<Map> list = new ArrayList<>();
        try {
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.size(30);
            searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
            searchSourceBuilder.sort(new FieldSortBuilder("occurCount.keyword").order(SortOrder.DESC));
            searchSourceBuilder.fetchSource(new String[]{"occurCount", type}, new String[]{});
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) - 12);
            searchSourceBuilder.query(QueryBuilders.boolQuery()
                    .must(QueryBuilders.rangeQuery("logDate").from(MWUtils.getUtcTime(MWUtils.getDate(calendar.getTime(), "yyyy-MM-dd HH:mm:ss"))).to(MWUtils.getUtcTime(MWUtils.getDate(Calendar.getInstance().getTime(), "yyyy-MM-dd HH:mm:ss")))));

            SearchRequest searchRequest = new SearchRequest();
            searchRequest.source(searchSourceBuilder);
            SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            HashMap<String, Integer> source = new HashMap();

            for (SearchHit searchHit : search.getHits().getHits()) {
                log.info("SearchHit");
                String sourceIp = searchHit.getSourceAsMap().get(type).toString();
                Object occurCount0 = searchHit.getSourceAsMap().get("occurCount");
                log.info("occurCount0{}", occurCount0);
                if (null != occurCount0 && StringUtils.isNotEmpty(occurCount0.toString()) && Pattern.compile("^-?[0-9]+").matcher(occurCount0.toString()).matches()) {
                    Integer occurCount = Integer.valueOf(occurCount0.toString());
                    log.info("source.get(sourceIp){}", source.get(sourceIp));
                    if (null != source.get(sourceIp) && StringUtils.isNotEmpty(source.get(sourceIp).toString())) {
                        Integer sourceCount = Integer.valueOf(source.get(sourceIp).toString());
                        source.put(sourceIp, occurCount + sourceCount);
                    } else {
                        source.put(sourceIp, occurCount);
                    }
                }
            }
            Map<String, Integer> MapDesc = MapSortUtil.sortByValueDesc(source);
            Set<Map.Entry<String, Integer>> entries = MapDesc.entrySet();
            entries.forEach(set -> {
                HashMap map = new HashMap();
                map.put("IP", set.getKey());
                map.put("value", set.getValue());
                list.add(map);
            });
            return list;
        } catch (IOException | ParseException e) {
            log.error("(getOccurCount)IO异常{}", e.getMessage());
            return null;
        }
    }

    public List<Map> getNewEvent() {
        List<Map> list = new ArrayList<>();
        try {
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.size(10);
            searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
            searchSourceBuilder.sort(new FieldSortBuilder("logDate").order(SortOrder.DESC));
            searchSourceBuilder.fetchSource(new String[]{"logDate", "alertLevel", "destIp", "sourceIp"}, new String[]{});

            SearchRequest searchRequest = new SearchRequest();
            searchRequest.source(searchSourceBuilder);
            SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            for (SearchHit searchHit : search.getHits().getHits()) {
                Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                list.add(sourceAsMap);
            }
            return list;
        } catch (Exception e) {
            log.error("(getNewEvent)IO异常{}", e);
            return null;
        }
    }


    public TodayDataListDto getTodayDataList(FilterAssetsParam filterAssetsParam) {
        TodayDataListDto todayDataListDto = new TodayDataListDto();
        List<Map<String, Object>> list = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        int hour = calendar.get(calendar.HOUR_OF_DAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.set(Calendar.MINUTE, 0);
        calendar2.set(Calendar.SECOND, 0);
        int time = 0;
        String units = "";
        MwCommonAssetsDto mwCommonAssetsDto = dao.getFilterAssets(filterAssetsParam);
        if (null == mwCommonAssetsDto) {
            //  mwCommonAssetsDto = MwCommonAssetsDto.builder().userId(filterAssetsParam.getUserId()).monitorMode(11).build();
            mwCommonAssetsDto = MwCommonAssetsDto.builder().userId(filterAssetsParam.getUserId()).build();
        }
        List<String> hostIps = mwAssetsManager.getLogHostList(mwCommonAssetsDto);
        if (hostIps.size() > 0) {
            BoolQueryBuilder queryBuilder2 = QueryBuilders.boolQuery();
            for (String hostIp : hostIps) {
                queryBuilder2 = queryBuilder2.should(QueryBuilders.termQuery("host.name", hostIp));
                queryBuilder2 = queryBuilder2.should(QueryBuilders.termQuery("fields.host", hostIp));
            }
            ThreadPoolExecutor executorService = new ThreadPoolExecutor(12, 20, 10, TimeUnit.MINUTES, new LinkedBlockingQueue<>());

            while (hour > time) {
                try {
                    CountRequest countRequest = new CountRequest();
                    QueryBuilder queryBuilder = QueryBuilders.boolQuery()
                            .must(QueryBuilders.rangeQuery(TIMESTAMP)
                                    .from(MWUtils.getUtcTime(MWUtils.getDate(calendar.getTime(), "yyyy-MM-dd HH:mm:ss")))
                                    .to(MWUtils.getUtcTime(MWUtils.getDate(calendar2.getTime(), "yyyy-MM-dd HH:mm:ss"))))
                            .must(queryBuilder2);
                    countRequest.query(queryBuilder);
                    CountResponse countResponse = restHighLevelClient.count(countRequest, RequestOptions.DEFAULT);
                    long countValue = countResponse.getCount();
                    Map<String, Object> map = new HashMap<>();
                    if (time == 0) {
                        Map<String, String> map1 = UnitsUtil.getValueAndUnits(String.valueOf(countValue), NewUnits.T.getUnits());
                        units = map1.get("units").toString();
                    }
                    Map<String, String> map2 = UnitsUtil.getValueMap(String.valueOf(countValue), units, NewUnits.T.getUnits());
                    map.put("date", calendar2.getTime().toString().substring(11, 19));
                    map.put("value", map2.get("value"));
                    list.add(map);
                    calendar2.set(Calendar.HOUR_OF_DAY, calendar2.get(Calendar.HOUR_OF_DAY) - 1);
                    time++;
                } catch (ParseException | IOException e) {
                    log.error("AlertTodayHistory_ParseException|IOException{}", e);
                    return todayDataListDto;
                }
            }
//        alertTodayHistory.setDate(dates);
//        alertTodayHistory.setCount(count);
            Collections.reverse(list);
            todayDataListDto.setList(list);
            todayDataListDto.setUnits(units);
        }
        return todayDataListDto;
    }

    public List<Map<String, Object>> getItemValue(FilterAssetsParam filterAssetsParam) {
        List<Map<String, Object>> list = new ArrayList<>();
        MwCommonAssetsDto mwCommonAssetsDto = dao.getFilterAssets(filterAssetsParam);
        if (null == mwCommonAssetsDto) {
            mwCommonAssetsDto = MwCommonAssetsDto.builder().userId(filterAssetsParam.getUserId()).build();
        }
        Map<String, Object> mapAssets = mwAssetsManager.getAssetsByUserId(mwCommonAssetsDto);
        if(StringUtils.isNotBlank(mwCommonAssetsDto.getFilterOrgId())&&mapAssets!=null){
            mapAssets=getAssetByOrgId(mapAssets,mwCommonAssetsDto.getFilterOrgId());
        }
        Object assetsList=null;
        if(mapAssets!=null){
            assetsList = mapAssets.get("assetsList");
        }
        if (null != assetsList) {
            List<MwTangibleassetsTable> mwTangibleassetsDTOS = (List<MwTangibleassetsTable>) assetsList;
            if (mwTangibleassetsDTOS.size() == 1) {
                MwTangibleassetsTable mwTangibleassetsTable = mwTangibleassetsDTOS.get(0);
                List<String> itemNames = new ArrayList<>();
                itemNames.add(ZbxConstants.CPU_UTILIZATION);
                itemNames.add(ZbxConstants.MEMORY_UTILIZATION);
                itemNames.add(ZbxConstants.DISK_UTILIZATION);
                itemNames.add(ZbxConstants.MW_DISK_USED);
                itemNames.add(ZbxConstants.MW_DISK_FREE);
                itemNames.add(ZbxConstants.MEMORY_USED);
                itemNames.add(ZbxConstants.MEMORY_FREE);

                Integer serverId = mwTangibleassetsTable.getMonitorServerId();
                String assetsId = mwTangibleassetsTable.getAssetsId();
                MWZabbixAPIResult result = mwtpServerAPI.itemGetbySearch(serverId, itemNames, assetsId);
                if (result.getCode() == 0) {
                    JsonNode jsonNode = (JsonNode) result.getData();
                    if (jsonNode.size() > 0) {
                        jsonNode.forEach(jsonNode1 -> {
                            HashMap map = new HashMap();
                            String name = jsonNode1.get("name").asText();
                            String lastValue = jsonNode1.get("lastvalue").asText();
                            Map<String, String> map1 = UnitsUtil.getValueAndUnits(lastValue, jsonNode1.get("units").asText());
                            double value = Double.parseDouble(map1.get("value"));
                            map.put("itemName", name);
                            if (name.endsWith("UTILIZATION")) {
                                map.put("lastValue", value);
                            } else {
                                Map<String, String> valueAndUnits = UnitsUtil.getValueAndUnits(String.valueOf(lastValue), NewUnits.B.getUnits());
                                String last = valueAndUnits.get("value") + valueAndUnits.get("units");
                                map.put("lastValue", last);
                            }
                            list.add(map);
                        });

                    }
                }
            }
        }
        return list;

    }


    public ItemRank getHostCountByLog(FilterAssetsParam filterAssetsParam) {
        MwCommonAssetsDto mwCommonAssetsDto = dao.getFilterAssets(filterAssetsParam);
        if (null == mwCommonAssetsDto) {
            //   mwCommonAssetsDto = MwCommonAssetsDto.builder().userId(filterAssetsParam.getUserId()).monitorMode(11).build();
            mwCommonAssetsDto = MwCommonAssetsDto.builder().userId(filterAssetsParam.getUserId()).build();
        }
        if(debug){
            log.info("/getHostCountByLog/browse:mwAssetsManager.getLogHostList start"+new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS").format(new Date()));
        }
//        List<String> logHostList = mwAssetsManager.getLogHostList(mwCommonAssetsDto);
        List<String> logHostList=mwAssetsManager.getLogHostList1(mwCommonAssetsDto);
        if(debug){
            log.info("/getHostCountByLog/browse:mwAssetsManager.getLogHostList end"+new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS").format(new Date()));
        }
        ItemRank itemRank = new ItemRank();
        List<TitleRank> titleRanks = new ArrayList<>();
        TitleRank titleRank1 = TitleRank.builder().name("日志数量").fieldName("value").build();
        TitleRank titleRank2 = TitleRank.builder().name("IP地址").fieldName("ip").build();
        titleRanks.add(titleRank1);
        titleRanks.add(titleRank2);
        itemRank.setTitleRanks(titleRanks);
        List<String> node = new ArrayList<>();
        node.add("日志数量");
        node.add("IP地址");
        itemRank.setTitleNode(node);
        List<ItemNameRank> list = new ArrayList<>();
        if (logHostList.size() > 0) {
            if(debug){
                log.info("/getHostCountByLog/browse:restHighLevelClient start"+new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS").format(new Date()));
            }
            ExecutorService executorService = Executors.newFixedThreadPool(20);
            List<Future<ItemNameRank>> futureList =new ArrayList<>();
            for (String hostIp : logHostList) {
                String[] split = hostIp.split(",");
                String hp = split[0];
                String sid = split[1];
                GetDataByCallable<ItemNameRank> getDataByCallable =new GetDataByCallable<ItemNameRank>() {
                    @Override
                    public ItemNameRank call() throws Exception {
                        CountRequest countRequest = new CountRequest();
                        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                        searchSourceBuilder.query(QueryBuilders.boolQuery()
                                .must(QueryBuilders.boolQuery().should(QueryBuilders.termQuery("host.name", hp))
                                        .should(QueryBuilders.termQuery("fields.host", hp))));
                        countRequest.source(searchSourceBuilder);
//            countRequest.query(QueryBuilders.boolQuery()
//                    .must(QueryBuilders.boolQuery().should(QueryBuilders.termQuery("host.name", hostIp))
//                            .should(QueryBuilders.termQuery("fields.host", hostIp))));

                        CountResponse count = restHighLevelClient.count(countRequest, RequestOptions.DEFAULT);
                        ItemNameRank map = new ItemNameRank();
                        map.setId(sid);
                        map.setName(hp);
                        map.setIp(hp);
                        long count1 = count.getCount();
                        map.setValue(UnitsUtil.getValueWithUnits(String.valueOf(count1), NewUnits.T.getUnits()));
                        map.setLastValue(Double.valueOf(UnitsUtil.getValueAndUnits(String.valueOf(count1), NewUnits.T.getUnits()).get("value")));
                        map.setUnits(UnitsUtil.getValueAndUnits(String.valueOf(count1), NewUnits.T.getUnits()).get("units"));
                        return map;
                    }

                };
                if(null!=getDataByCallable){
                    Future<ItemNameRank> f = executorService.submit(getDataByCallable);
                    futureList.add(f);
                }
            }
            futureList.forEach(f->{
                try {
                    ItemNameRank itemNameRank = f.get(30, TimeUnit.SECONDS);
                    list.add(itemNameRank);
                } catch (Exception e) {
                    f.cancel(true);
                    executorService.shutdown();
                }
            });
            executorService.shutdown();
            if(debug){
                log.info("/getHostCountByLog/browse:restHighLevelClient end"+new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS").format(new Date()));
            }
        }
        itemRank.setItemNameRankList(list);
        return itemRank;
    }


    public List<Map<String, Object>> getHostCountByLog1() {
        List<Map<String, Object>> list = new ArrayList<>();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(100);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.DESC));
        searchSourceBuilder.fetchSource(new String[]{"host"}, new String[]{});
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        try {
            MatchAllQueryBuilder queryBuilder = QueryBuilders.matchAllQuery().queryName("host");
            searchSourceBuilder.query(queryBuilder);
            searchSourceBuilder.query(QueryBuilders.boolQuery()
                    .must(queryBuilder)
                    .must(QueryBuilders.rangeQuery("@timestamp").from(MWUtils.getUtcTime(MWUtils.getDate(calendar.getTime(), "yyyy-MM-dd HH:mm:ss"))).to(MWUtils.getUtcTime(MWUtils.getDate(Calendar.getInstance().getTime(), "yyyy-MM-dd HH:mm:ss")))));
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.source(searchSourceBuilder);
            SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            HashSet<String> set = new HashSet();
            for (SearchHit searchHit : search.getHits().getHits()) {
                Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                String host = sourceAsMap.get("host").toString();
                if (null != host && StringUtils.isNotEmpty(host)) {
                    set.add(host);
                }
            }
            for (String host : set) {
                HashMap<String, Object> map = new HashMap<>();
                CountRequest countRequest = new CountRequest();
                countRequest.query(QueryBuilders.boolQuery()
                        .filter(QueryBuilders.matchQuery("host", host))
                        .filter(QueryBuilders.rangeQuery("@timestamp").from(MWUtils.getUtcTime(MWUtils.getDate(calendar.getTime(), "yyyy-MM-dd HH:mm:ss")))
                                .to(MWUtils.getUtcTime(MWUtils.getDate(Calendar.getInstance().getTime(), "yyyy-MM-dd HH:mm:ss")))));
                CountResponse count = restHighLevelClient.count(countRequest, RequestOptions.DEFAULT);
                long count1 = count.getCount();
                map.put("IP", host);
                map.put("count", UnitsUtil.getValueWithUnits(String.valueOf(count1), NewUnits.T.getUnits()));
                list.add(map);
            }
        } catch (ParseException | IOException e) {
            log.error("getHostCountByLog_ParseException | IOException{}", e);
            return list;
        }

        return list;
    }
    @Transactional
    public List<IndexBulk> getIndexModelBase(Integer userId){
        List<IndexModelBase> list=new ArrayList<>();
        List<IndexBulk> bulkList=new ArrayList<>();
        List<IndexBulk> result=new ArrayList<>();
        try {

             result = indexDao.selectBulkByUser(userId);
            int count = indexDao.selectBulkCount(userId);
            if(result.size()==0&&count==0){
                list = indexDao.getPageSelectBase2();
                for (int i = 0; i < list.size(); i++) {
                    String modelDateId = UuidUtil.getUid();
                    //初始化数据只需要9个模块
                    Integer bulkId = list.get(i).getBulkId();
                    if(bulkId == null || bulkId == 10 || bulkId == 11 || bulkId == 12 || bulkId == 13)continue;
                    bulkList.add(new IndexBulk(modelDateId,list.get(i).getBulkId(),list.get(i).getBulkName(),userId));
                }

                indexDao.insertIndexBulk(bulkList);
                result = indexDao.selectBulkByUser(userId);
            }

//            list = indexDao.getIndexBase();
        } catch (Exception e) {
            log.error("getIndexModelBase{}",e);
        }
        return result;
    }

    public List<IndexModelBase> getPageSelectBase(){
        List<IndexModelBase> list= null;
        try {
//            list = indexDao.getPageSelectBase();
            list = indexDao.getPageSelectBase2();

        } catch (Exception e) {
            log.error("getIndexModelBase{}",e);
        }
        return list;
    }

    public List getLagerScreenLinkLineChart(String linkId) {
        try {
            List data = new ArrayList();
            //根据线路ID查询线路信息
            //设置线路查询信息
            LinkDropDownParam linkDropDownParam = new LinkDropDownParam();
            linkDropDownParam.setLinkId(linkId);
            Map pubCriteria = PropertyUtils.describe(linkDropDownParam);
            List<NetWorkLinkDto> netWorkLinkDtos = mwNetWorkLinkDao.getPubLinkList(pubCriteria);
            if(CollectionUtils.isEmpty(netWorkLinkDtos))return data;
            NetWorkLinkDto workLinkDto = netWorkLinkDtos.get(0);
            //根据线路数据获取取值端口及服务器ID信息
            Map<String, Object> zabbxiNewsMap = getLinkNews(workLinkDto);
            if(zabbxiNewsMap.isEmpty())return data;
            //组合参数查询折线图信息
            ServerHistoryDto historyDto = new ServerHistoryDto();
            historyDto.setAssetsId(zabbxiNewsMap.get("assetsId").toString());
            historyDto.setMonitorServerId(Integer.parseInt(zabbxiNewsMap.get("serverId").toString()));
            historyDto.setDateType(1);
            historyDto.setValueType("AVG");
            List<String> itemNames = new ArrayList<>();
            itemNames.add("["+zabbxiNewsMap.get("port").toString()+"]MW_INTERFACE_IN_TRAFFIC");
            itemNames.add("["+zabbxiNewsMap.get("port").toString()+"]MW_INTERFACE_OUT_TRAFFIC");
            historyDto.setName(itemNames);
            Reply historyData = serverService.getHistoryData(historyDto);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            data = (List) historyData.getData();
            //转换时间
            if(!CollectionUtils.isEmpty(data)){
                for (Object d : data) {
                    List data2 = (List) d;
                    if(!CollectionUtils.isEmpty(data2)){
                        for (Object o : data2) {
                            Map<String,Object> map = (Map<String, Object>) o;
                            map.put("linkName",workLinkDto.getLinkName());
                            Object realData = map.get("realData");
                            if(realData != null){
                                List<MWItemHistoryDto> dtos = (List<MWItemHistoryDto>) realData;
                                if(!CollectionUtils.isEmpty(dtos)){
                                    for (MWItemHistoryDto dto : dtos) {
                                        dto.setNs(format.format(dto.getDateTime()).substring(0,16));
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return data;
        } catch (Exception e) {
            log.error("fail to insert with getLagerScreenLinkLineChart={}, cause:{}", e.getMessage());
            return null;
        }

    }

    /**
     * 获取线路信息
     * @param workLinkDto
     */
    private Map<String,Object> getLinkNews(NetWorkLinkDto workLinkDto){
        String valuePort = workLinkDto.getValuePort();
        Map<String,Object> map = new HashMap<>();
        String baseLinkHostId = "";
        Integer baseLinkServerId = null;
        String port = "";
        if(StringUtils.isBlank(valuePort))return map;
        if (valuePort.equals("ROOT")) {
            baseLinkHostId = workLinkDto.getRootAssetsParam().getAssetsId();
            baseLinkServerId = workLinkDto.getRootAssetsParam().getMonitorServerId() == null ? null : workLinkDto.getRootAssetsParam().getMonitorServerId();
            port = workLinkDto.getRootPort();
        } else {
            baseLinkHostId = workLinkDto.getTargetAssetsParam().getAssetsId();
            baseLinkServerId = workLinkDto.getTargetAssetsParam().getMonitorServerId() == null ? null : workLinkDto.getTargetAssetsParam().getMonitorServerId();
            port = workLinkDto.getTargetPort();
        }
        if(StringUtils.isBlank(baseLinkHostId) || baseLinkServerId == null || StringUtils.isBlank(port))return map;
        map.put("assetsId",baseLinkHostId);
        map.put("serverId",baseLinkServerId);
        map.put("port",port);
        return map;
    }

    /**
     * 查询zabbix中的流量信息
     * @param names
     * @param filterAssetsParam
     */
    public ItemRank  getScreenFlowNews(List<String> names, FilterAssetsParam filterAssetsParam
            , Map<Integer, List<String>> map, Integer count) throws Exception{
        ItemRank itemRank = new ItemRank();
        if(map == null){
            map = getAssetIdsByServerId(filterAssetsParam);
        }
        //查询资产信息
        Map<String, AssetsDto> assetsDtoMap = getAssetsNews(map);
        Map<String,ItemNameRank> rankMap = new HashMap<>();
        if(assetsDtoMap == null || assetsDtoMap.isEmpty())return itemRank;
        for (String name : names) {
            for (Integer key : map.keySet()){
                //查询zabbix数据
                List<String> hostIds = map.get(key);
                MWZabbixAPIResult result = mwtpServerAPI.itemGetbyType(key, name, hostIds);
                if (result != null && result.code == 0){
                    JsonNode itemData = (JsonNode) result.getData();
                    if (itemData.size() > 0){
                        for (JsonNode node : itemData) {
                            String hostId = node.get("hostid").asText();
                            int i = node.get("name").asText().indexOf("]");
                            ItemNameRank itemNameRank = new ItemNameRank();
                            if(rankMap.containsKey(hostId+(node.get("name").asText().substring(1, i)))){
                                itemNameRank = rankMap.get(hostId+(node.get("name").asText().substring(1, i)));
                            }
                            if(assetsDtoMap.get(hostId) == null)continue;
                            AssetsDto assets = assetsDtoMap.get(hostId);
                            if (i != -1) {
                                itemNameRank.setType(node.get("name").asText().substring(1, i));
                            }
                            itemNameRank.setId(assets.getId());
                            itemNameRank.setName(assets.getAssetsName());
                            itemNameRank.setIp(assets.getAssetsIp());
                            itemNameRank.setAssetsId(assets.getAssetsId());
                            itemNameRank.setMonitorServerId(assets.getMonitorServerId());
                            itemNameRank.setUrl(assets.getUrl());
                            itemNameRank.setParam(assets.getParam());
                            String lastvalue = node.get("lastvalue").asText();
                            Double sortTotalValue = itemNameRank.getSortTotalValue();
                            if(sortTotalValue == null){
                                itemNameRank.setSortTotalValue(Double.parseDouble(lastvalue));
                            }else{
                                itemNameRank.setSortTotalValue(sortTotalValue+Double.parseDouble(lastvalue));
                            }
                            Map<String, String> map1 = UnitsUtil.getValueAndUnits(lastvalue, node.get("units").asText());
                            Map<String, String> map2 = UnitsUtil.getConvertedValue(new BigDecimal(lastvalue), node.get("units").asText());
                            if (null != map2) {
                                String dataUnits = map2.get("units");
                                String v = UnitsUtil.getValueMap(lastvalue, dataUnits, node.get("units").asText()).get("value");
                                Double values = new BigDecimal(v).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                                if(name.contains(ScreenConstant.INTERFACE_IN)){
                                    itemNameRank.setAcceptLastValue(values);
                                    itemNameRank.setAcceptStrLastValue(map2.get("value")+dataUnits);
                                }
                                if(name.contains(ScreenConstant.INTERFACE_OUT)){
                                    itemNameRank.setSendLastValue(values);
                                    itemNameRank.setSendStrLastValue(map2.get("value")+dataUnits);
                                }
                            }
                            rankMap.put(hostId+(node.get("name").asText().substring(1, i)),itemNameRank);
                        }
                    }
                }
            }
        }
        //组合数据
        if(rankMap == null || rankMap.isEmpty())return itemRank;
        List<ItemNameRank> ranks = new ArrayList<>();
        for (String key : rankMap.keySet()) {
            ranks.add(rankMap.get(key));
        }
        //流量带宽排序
        if(names.contains("INTERFACE_IN_TRAFFIC") || names.contains("INTERFACE_OUT_TRAFFIC")){
            if(CollectionUtils.isNotEmpty(ranks)){
                Collections.sort(ranks, new Comparator<ItemNameRank>() {
                    @Override
                    public int compare(ItemNameRank o1, ItemNameRank o2) {
                        if(o2.getSortTotalValue()>o1.getSortTotalValue()){
                            return 1;
                        }
                        if(o2.getSortTotalValue()<o1.getSortTotalValue()){
                            return -1;
                        }
                        return 0;
                    }
                });
            }
        }else{
            //排序
            if(CollectionUtils.isNotEmpty(ranks)){
                Collections.sort(ranks, new Comparator<ItemNameRank>() {
                    @Override
                    public int compare(ItemNameRank o1, ItemNameRank o2) {
                        if(o2.getSendLastValue()+o2.getAcceptLastValue()>o1.getSendLastValue()+o1.getAcceptLastValue()){
                            return 1;
                        }
                        if(o2.getSendLastValue()+o2.getAcceptLastValue()<o1.getSendLastValue()+o1.getAcceptLastValue()){
                            return -1;
                        }
                        return 0;
                    }
                });
            }
        }

        if(count != null && ranks.size() > count){
            ranks = ranks.subList(0,count);
        }
        String [] titleNode = new String[]{"资产名称","接口名称","流量(入)","流量(出)"};
        TitleRank titleRank1 = TitleRank.builder().name("资产名称").fieldName("name").build();
        TitleRank titleRank2 = TitleRank.builder().name("接口名称").fieldName("type").build();
        TitleRank titleRank3 = TitleRank.builder().name("流量(入)").fieldName("acceptStrLastValue").build();
        TitleRank titleRank4 = TitleRank.builder().name("流量(出)").fieldName("sendStrLastValue").build();
        List<TitleRank> titleRanks = new ArrayList<>();
        titleRanks.add(titleRank1);
        titleRanks.add(titleRank2);
        titleRanks.add(titleRank3);
        titleRanks.add(titleRank4);
        itemRank.setItemNameRankList(ranks);
        itemRank.setTitleNode(Arrays.asList(titleNode));
        itemRank.setTitleRanks(titleRanks);
        return itemRank;
    }


    /**
     * 获取资产信息
     * @param map
     */
    private Map<String,AssetsDto> getAssetsNews(Map<Integer, List<String>> map) throws Exception{
        Map<String,AssetsDto> assetsDtoMap = new HashMap<>();
        //查询资产信息
        if(map == null || map.isEmpty())return assetsDtoMap;
        List<String> allAseetsIds = new ArrayList<>();
        for (Integer key : map.keySet()){
            List<String> assetsIds = map.get(key);
            if(CollectionUtils.isEmpty(assetsIds)){continue;}
            for (String assetsId : assetsIds) {
                if(StringUtils.isBlank(assetsId)){continue;}
                allAseetsIds.add(assetsId);
            }
        }
        log.info("MWModelManage{} getAssetsNews() allAseetsIds:::"+allAseetsIds);
        List<AssetsDto> assetsByIds = mwModelViewCommonService.getAssetsByIds(allAseetsIds);
        log.info("MWModelManage{} getAssetsNews() assetsByIds:::"+assetsByIds);
        if(CollectionUtils.isEmpty(assetsByIds))return assetsDtoMap;
        for (AssetsDto assetsById : assetsByIds) {
            assetsDtoMap.put(assetsById.getAssetsId(),assetsById);
        }
        return assetsDtoMap;
    }

    @Autowired
    private MWAlertService mwalertService;

    /**
     * 获取当前告警数据
     */
    public Map<String,Object> getCurrGiveAnAlarm(Integer userId){
        Map<String,Object> map = new HashMap<>();
        AlertParam alertParam = new AlertParam();
        alertParam.setPageSize(1000);
        alertParam.setPageNumber(1);
        alertParam.setUserId(userId);
        Reply reply = mwalertService.getCurrAlertPage(alertParam);
        log.info("查询大屏告警信息3"+reply+"参数："+alertParam);
        if (null != reply && reply.getRes() == PaasConstant.RES_SUCCESS){
            PageInfo pageInfo = (PageInfo) reply.getData();
            List list = pageInfo.getList();
            map.put("alterData",list);
        }
        getAllAlterClassify(reply,map);
        log.info("查询大屏告警信息4"+map+"参数："+alertParam);
        return map;
    }

    /**
     * 查询告警分类
     */
    private void getAllAlterClassify(Reply reply,Map<String,Object> map){
        Map<String,Integer> data = new HashMap<>();
        if(reply != null && reply.getRes() == PaasConstant.RES_SUCCESS){
            PageInfo pageInfo = (PageInfo) reply.getData();
            List<ZbxAlertDto> list = pageInfo.getList();
            if(CollectionUtils.isNotEmpty(list)){
                for (ZbxAlertDto zbxAlertDto : list) {
                    String severity = zbxAlertDto.getSeverity();
                    if(data.containsKey(severity)){
                        Integer count = data.get(severity);
                        data.put(severity,count+1);
                    }else{
                        data.put(severity,1);
                    }
                }
            }
            data.put("全部",list.size());
        }
        List<ActivityAlertClassifyDto> list = new ArrayList<>();
        if(!data.isEmpty()){
            for (String key : data.keySet()) {
                ActivityAlertClassifyDto dto = new ActivityAlertClassifyDto();
                Integer num = data.get(key);
                if("全部".equals(key)){
                    dto.setValue(0);
                    dto.setLabel(key);
                    dto.setNum(num);
                    list.add(dto);
                }
                if("信息".equals(key)){
                    dto.setValue(2);
                    dto.setLabel(key);
                    dto.setNum(num);
                    list.add(dto);
                }
                if("警告".equals(key)){
                    dto.setValue(3);
                    dto.setLabel(key);
                    dto.setNum(num);
                    list.add(dto);
                }
                if("严重".equals(key)){
                    dto.setValue(4);
                    dto.setLabel(key);
                    dto.setNum(num);
                    list.add(dto);
                }
                if("紧急".equals(key)){
                    dto.setValue(5);
                    dto.setLabel(key);
                    dto.setNum(num);
                    list.add(dto);
                }
                if("一般".equals(key)){
                    dto.setValue(1);
                    dto.setLabel(key);
                    dto.setNum(num);
                    list.add(dto);
                }
            }
        }
        if(!data.containsKey("全部")){
            ActivityAlertClassifyDto dto = new ActivityAlertClassifyDto();
            dto.setValue(0);
            dto.setLabel("全部");
            dto.setNum(0);
            list.add(dto);
        }
        if(!data.containsKey("信息")){
            ActivityAlertClassifyDto dto = new ActivityAlertClassifyDto();
            dto.setValue(2);
            dto.setLabel("信息");
            dto.setNum(0);
            list.add(dto);
        }
        if(!data.containsKey("警告")){
            ActivityAlertClassifyDto dto = new ActivityAlertClassifyDto();
            dto.setValue(3);
            dto.setLabel("警告");
            dto.setNum(0);
            list.add(dto);
        }
        if(!data.containsKey("严重")){
            ActivityAlertClassifyDto dto = new ActivityAlertClassifyDto();
            dto.setValue(4);
            dto.setLabel("严重");
            dto.setNum(0);
            list.add(dto);
        }
        if(!data.containsKey("紧急")){
            ActivityAlertClassifyDto dto = new ActivityAlertClassifyDto();
            dto.setValue(5);
            dto.setLabel("紧急");
            dto.setNum(0);
            list.add(dto);
        }
        if(!data.containsKey("一般")){
            ActivityAlertClassifyDto dto = new ActivityAlertClassifyDto();
            dto.setValue(1);
            dto.setLabel("一般");
            dto.setNum(0);
            list.add(dto);
        }
        if(CollectionUtils.isNotEmpty(list)){
            Collections.sort(list, new Comparator<ActivityAlertClassifyDto>() {
                @Override
                public int compare(ActivityAlertClassifyDto o1, ActivityAlertClassifyDto o2) {
                    if(o1.getValue() > o2.getValue()){
                        return 1;
                    }
                    if(o1.getValue() < o2.getValue()){
                        return -1;
                    }
                    return 0;
                }
            });
        }
        map.put("warnList",list);
    }
}
