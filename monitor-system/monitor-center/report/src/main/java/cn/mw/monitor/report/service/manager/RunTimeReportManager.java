package cn.mw.monitor.report.service.manager;

import cn.mw.monitor.alert.service.manager.MWAlertManager;
import cn.mw.monitor.api.common.SpringUtils;
import cn.mw.monitor.common.constant.ZabbixItemConstant;
import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.report.dao.MwReportDao;
import cn.mw.monitor.report.dao.MwReportTerraceManageDao;
import cn.mw.monitor.report.dto.HistoryValueDto;
import cn.mw.monitor.report.dto.TrendDto;
import cn.mw.monitor.report.dto.assetsdto.AssetByTypeDto;
import cn.mw.monitor.report.dto.assetsdto.PeriodTrendDto;
import cn.mw.monitor.report.dto.assetsdto.RunTimeItemValue;
import cn.mw.monitor.report.dto.assetsdto.RunTimeQueryParam;
import cn.mw.monitor.report.service.GetDataByThread;
import cn.mw.monitor.report.service.detailimpl.ReportUtil;
import cn.mw.monitor.report.util.ReportDateUtil;
import cn.mw.monitor.server.service.impl.MwServerManager;
import cn.mw.monitor.service.assets.model.MwCommonAssetsDto;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.assets.param.QueryTangAssetsParam;
import cn.mw.monitor.service.common.MWDateConstant;
import cn.mw.monitor.service.server.api.dto.MWItemHistoryDto;
import cn.mw.monitor.service.server.param.QueryAssetsAvailableParam;
import cn.mw.monitor.service.user.api.MWUserCommonService;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.state.DateTimeTypeEnum;
import cn.mw.monitor.user.service.MWUserService;
import cn.mw.monitor.util.MWUtils;
import cn.mw.monitor.util.RedisUtils;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mw.zbx.manger.MWWebZabbixManger;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.jdbc.Null;
import org.omg.PortableInterceptor.INACTIVE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class RunTimeReportManager {
    public static final String CPU_UTILIZATION = "CPU_UTILIZATION";
    public static final String MEMORY_UTILIZATION = "MEMORY_UTILIZATION";
    public static final String MEMORY_TOTAL = "MEMORY_TOTAL";
    public static final String MEMORY_USED = "MEMORY_USED";
    public static final String ICMP_RESPONSE_TIME = "ICMP_RESPONSE_TIME";
    public static final String MEMORY_FREE = "MEMORY_FREE";

    @Value("${report.debug}")
    private boolean debug;

    @Value("${report.hostGroupSize}")
    private int hostGroupSize;


    @Value("${report.runTimeStatus.cpuFen}")
    private Integer cpuNum;

    @Value("${report.runTimeStatus.diskFen}")
    private Integer diskNum;

    @Value("${report.runTimeStatus.icmpFen}")
    private Integer icmpNum;

    @Value("${report.runTimeStatus.interfaceFen}")
    private Integer interfaceNum;

    @Value("${report.runTimeStatus.memoryFen}")
    private Integer memoryNum;

    @Value("${report.history.group}")
    private Integer hisToryGroup;

    @Autowired
    RunTimeReportManager runTimeReportManager;
    @Autowired
    private MWUserCommonService mwUserCommonService;
    @Autowired
    private MWTPServerAPI mwtpServerAPI;
    @Autowired
    private MwAssetsManager mwAssetsManager;
    @Autowired
    private MwServerManager mwServerManager;
    @Autowired
    private MWWebZabbixManger zabbixManger;
    @Resource
    private MwReportDao mwReportDao;
    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;
    @Autowired
    private MWAlertManager alertManager;
    @Autowired
    private MwReportTerraceManageDao terraceManageDao;


    public List<RunTimeItemValue> getrunTimeMemory3(Integer userId, String name, Long startTime, Long endTime) {
        MwCommonAssetsDto mwCommonAssetsDto = new MwCommonAssetsDto();
        mwCommonAssetsDto.setUserId(userId);
        Map<String, Object> map = mwAssetsManager.getAssetsByUserId(mwCommonAssetsDto);
        Object assetsList = map.get("assetsList");
        List<RunTimeItemValue> list = new ArrayList<>();
        if (assetsList != null) {
            List<MwTangibleassetsTable> mwTangibleassetsDTOS = (List<MwTangibleassetsTable>) assetsList;
            if (mwTangibleassetsDTOS.size() == 0) {
                return null;
            }
            CopyOnWriteArrayList<Future<RunTimeItemValue>> futureList = new CopyOnWriteArrayList<>();
            ExecutorService executorService = Executors.newFixedThreadPool(20);
            for (MwTangibleassetsTable mwTangibleassetsDTO : mwTangibleassetsDTOS) {
                GetDataByThread<RunTimeItemValue> getDataByThread = new GetDataByThread<RunTimeItemValue>() {
                    @Override
                    public RunTimeItemValue call() throws Exception {
                        RunTimeItemValue threadValue = getThreadValue3(mwTangibleassetsDTO, name, startTime, endTime);
                        return threadValue;
                    }
                };
                Future<RunTimeItemValue> submit = executorService.submit(getDataByThread);
                futureList.add(submit);
            }

            for (Future<RunTimeItemValue> itemValueFuture : futureList) {
                try {
                    RunTimeItemValue itemValue = itemValueFuture.get(30, TimeUnit.SECONDS);
                    list.add(itemValue);
                } catch (Exception e) {
                    itemValueFuture.cancel(true);
                    executorService.shutdown();
                } finally {
                    executorService.shutdown();
                }
            }
        }
        return list;
    }

    public List<RunTimeItemValue> getrunTimeMemory(Integer userId, String name, Long startTime, Long endTime, Integer type) {
        MwCommonAssetsDto mwCommonAssetsDto = new MwCommonAssetsDto();
        mwCommonAssetsDto.setUserId(userId);
        Map<String, Object> map = mwAssetsManager.getAssetsByUserId(mwCommonAssetsDto);
        Object assetsList = map.get("assetsList");
        List<RunTimeItemValue> list = new ArrayList<>();

        if (assetsList != null) {
            List<MwTangibleassetsTable> mwTangibleassetsDTOS = (List<MwTangibleassetsTable>) assetsList;
            if (mwTangibleassetsDTOS.size() == 0) {
                return null;
            }
            List<RunTimeItemValue> runTimeItemValues = getrunTimekill(mwTangibleassetsDTOS, name, startTime, endTime, type);;
            log.info("接口已加载"+runTimeItemValues.size());
            if (name.equals("INTERFACE_IN_UTILIZATION")){
                if (runTimeItemValues.size()==0){
                    List<RunTimeItemValue> kill =getrunTimekill(mwTangibleassetsDTOS,"INTERFACE_OUT_UTILIZATION", startTime, endTime, type);
                    list.addAll(kill);
                }
                if (runTimeItemValues.size()>0){
                    List<RunTimeItemValue> kill = getrunTimekill(mwTangibleassetsDTOS,"INTERFACE_OUT_UTILIZATION", startTime, endTime, type);
                    for (RunTimeItemValue runTimeItemValue:runTimeItemValues) {
                        for (RunTimeItemValue r:kill) {
                            if (r.getInterfaceName().equals(runTimeItemValue.getInterfaceName())&&r.getAssetName().equals(runTimeItemValue.getAssetName())){
                                runTimeItemValue.setOutInterfaceAvgValue(r.getAvgValue());
                            }
                        }
                    }
                }
            }

            log.info("接口已加载"+runTimeItemValues.size());
            list.addAll(runTimeItemValues);
        }
        return list;
    }

    private List<RunTimeItemValue> getrunTimekill(List<MwTangibleassetsTable> mwTangibleassetsDTOS, String name, Long startTime, Long endTime, Integer type) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Boolean tebie = false;
        if (name.equals("DISK_UTILIZATION")||name.equals("INTERFACE_IN_UTILIZATION")||name.equals("INTERFACE_OUT_UTILIZATION")){
            tebie=true;
        }

        Map<String, List<String>> map = new HashMap<>();
        RedisUtils redisUtils = SpringUtils.getBean(RedisUtils.class);
        List<RunTimeItemValue> runTimeItemValues = new ArrayList<>();

        Boolean changeTime = true;
        if (type == DateTimeTypeEnum.TODAY.getCode()) {
            changeTime = false;
        }


        //asstetsId - serverId
        Map<Integer, List<String>> maplist = new HashMap<>();
        for (MwTangibleassetsTable m : mwTangibleassetsDTOS) {
            if (maplist.get(m.getMonitorServerId()) == null) {
                List<String> strings = new ArrayList<>();
                strings.add(m.getAssetsId());
                maplist.put(m.getMonitorServerId(), strings);
            } else {
                List<String> strings = maplist.get(m.getMonitorServerId());
                strings.add(m.getAssetsId());
                maplist.put(m.getMonitorServerId(), strings);
            }
        }

        Map<Integer, Map<Integer, List<String>>> fenzu = new HashMap<>();
        //求 asstetsId - itemids
        for (Integer s : maplist.keySet()) {
            log.info("查询zabbix数据缓存"+s+"："+maplist.get(s));
            MWZabbixAPIResult result0 = mwtpServerAPI.itemGetbyType(s, name, maplist.get(s), false);
            if(result0 == null || result0.isFail())continue;
            log.info("查询zabbix数据结果缓存"+result0);
            JsonNode resultData = (JsonNode) result0.getData();
            if (resultData != null && resultData.size() > 0) {
                log.info("查出接口数据"+name+resultData.size());
                //组装查询条件和 返回数据
                for (int i = 0; i < resultData.size(); i++) {
                    String  nametable = "";
                    if (tebie){
                        nametable = "kehedisk"+resultData.get(i).get("name").asText().replace("DISK_UTILIZATION","");
                    }
                    if (resultData.get(i).get("hostid") == null) {
                        List<String> k = map.get(resultData.get(i).get("hostid").asText());
                        k.add(resultData.get(i).get("itemid").asText());
                        map.put(resultData.get(i).get("hostid").asText()+nametable, k);
                    } else {
                        List<String> k = new ArrayList<>();
                        if (resultData.get(i).get("itemid").asText() != null && resultData.get(i).get("itemid").asText().trim() != "") {
                            k.add(resultData.get(i).get("itemid").asText());
                            map.put(resultData.get(i).get("hostid").asText()+nametable, k);
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


        Map<String, List<String>> afterres = new HashMap<>();
        Map<String, List<String>> res = new HashMap<>();




        //新增五分钟缓存
        Boolean test = false;
        //redis来优化
        if (!changeTime) {
            Integer kill = 0;
            for (String s : map.keySet()) {
                Map<String, Object> redisMap = (Map<String, Object>) redisUtils.get(name+":"+s);
                if (redisMap!=null){
                    Long newtime = new Date().getTime()/1000;
                    Long l = (Long) redisMap.get("saveTime");
                    if (newtime-l<240){
                        test=true;
                    }
                    if (l>=startTime&&l<=endTime){
                        startTime=l;
                        List<String> doubles = new ArrayList<>();
                        List <String> strings = (List<String>) redisMap.get("listData");
                        for (int i = 0; i <strings.size() ; i++) {
                            doubles.add(strings.get(i));
                        }
                        res.put(s, doubles);
                    }
                }
            }

        }

        //加工数据 hostId -> res
        Integer fenzuNum = getNum(name);
        log.info("分组的数据"+name+":"+fenzuNum);
        if (!test){
            for (Integer i : fenzu.keySet()) {
                for (Integer j : fenzu.get(i).keySet()) {
                    List<String> k = fenzu.get(i).get(j);
                    Map<Integer,List<String>> fenK = getFenK(k,hisToryGroup);
                    for (Integer v:fenK.keySet()) {
                        List<String> x = fenK.get(v);
                        MWZabbixAPIResult historyRsult = mwtpServerAPI.HistoryGetByTimeAndType(i, x, startTime, endTime, j);
                        if(name.equals("ICMP_LOSS") && debug){
                            log.info("请求数据"+x.toString()+"type:"+j.toString());
                            log.info("数据结果"+name+":"+historyRsult.toString());
                        }
                        JsonNode resultData = (JsonNode) historyRsult.getData();
                        if (resultData != null && resultData.size() > 0) {
                            for (int l = 0; l < resultData.size(); l++) {
                                String itemid = resultData.get(l).get("itemid").asText();
                                long clock = resultData.get(l).get("clock").asLong();
                                if (afterres.get(itemid) == null) {
                                    List<String> strings = new ArrayList<>();
                                    if (resultData.get(l).get("value").asText().contains("E")&&resultData.get(l).get("value").asText().contains("e")){
                                        strings.add(0.0+"_"+clock);
                                    }else {
                                        strings.add(resultData.get(l).get("value").asDouble()+"_"+clock);
                                    }


                                    afterres.put(itemid, strings);
                                } else {
                                    List<String> strings = afterres.get(itemid);
                                    if (resultData.get(l).get("value").asText().contains("E")&&resultData.get(l).get("value").asText().contains("e")){
                                        strings.add(0.0+"_"+clock);
                                    }else {
                                        strings.add(resultData.get(l).get("value").asDouble()+"_"+clock);
                                    }

                                    afterres.put(itemid, strings);
                                }
                            }
                        }
                    }

                }
            }
            //加工数据 asstest -> res
            try {
                for (String s : map.keySet()) {
                    List<String> itemsIds = map.get(s);
                    List<String> doubles = new ArrayList<>();
                    for (String k : itemsIds) {
                        if (afterres.get(k) != null) {
                            doubles.addAll(afterres.get(k));
                        }
                    }
                    if (doubles.size()>0){
                        if (res.get(s)!=null){
                            doubles.addAll(res.get(s));
                        }
                        res.put(s, doubles);
                    }

                }
            }catch (Throwable e){
                log.error("查询数据失败，失败信息:"+e.getMessage());
            }
        }

        try {
            //计算数据
            for (String s : res.keySet()) {
                String  diskname = "";
                String  split ="";
                if (tebie){
                    split="kehedisk";
                    String [] k = s.split(split);
                    s= k[0];
                    diskname = k[1];
                }

                for (MwTangibleassetsTable m : mwTangibleassetsDTOS) {
                    if (m.getAssetsId().equals(s)) {

                        List<String> agvgs = res.get(s+split+diskname);
                        List<String> strings = new ArrayList<>();
                        List<Double> doubles = new ArrayList<>();
                        for (String d:agvgs) {
                            String[] s1 = d.split("_");
                            if (d.contains("e")||d.contains("E")||Double.parseDouble(s1[0])<0.01||Double.parseDouble(s1[0])>1000){
                                strings.add("0.0_"+startTime);
                                doubles.add(0.0);
                            }else {
                                doubles.add(Double.parseDouble(s1[0]));
                                strings.add(d);
                            }
                        }
                        Map<String, Object> redisMap = new HashMap<>();
                        redisMap.put("listData",strings);
                        redisMap.put("saveTime",new Date().getTime()/1000);
                        if (!test){
                            redisUtils.set(name+":"+s+split+diskname,redisMap,86400);
                        }
                        RunTimeItemValue runTimeItemValue = new RunTimeItemValue();



                        String valueavg = String.valueOf(doubles.stream().mapToDouble(Double::valueOf).average().getAsDouble());
                        if (valueavg.contains("e")||valueavg.contains("E")||Double.valueOf(valueavg)<0.01||Double.valueOf(valueavg)>1000){
                            strings.add("0.0");
                            valueavg="0.0";
                        }
                        String values = new BigDecimal(valueavg).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                        runTimeItemValue.setDiskName(diskname.equals("")?"":diskname.substring(diskname.indexOf("[")+1,diskname.indexOf("]")));
                        runTimeItemValue.setInterfaceName(diskname.equals("")?"":diskname.substring(diskname.indexOf("[")+1,diskname.indexOf("]")));
                        runTimeItemValue.setOutInterfaceAvgValue(values);
                        runTimeItemValue.setAvgValue(values);
                        runTimeItemValue.setMaxValue(String.valueOf(doubles.stream().mapToDouble(Double::valueOf).max().getAsDouble()));
                        runTimeItemValue.setMinValue(String.valueOf(doubles.stream().mapToDouble(Double::valueOf).min().getAsDouble()));
                        for (String valueAndTime : agvgs) {
                            String[] s1 = valueAndTime.split("_");
                            if(runTimeItemValue.getMaxValue().equals(s1[0])){
                                runTimeItemValue.setMaxValueTime(format.format(new Date(Long.parseLong(s1[1])*1000)));
                            }
                            if(runTimeItemValue.getMinValue().equals(s1[0])){
                                runTimeItemValue.setMinValueTime(format.format(new Date(Long.parseLong(s1[1])*1000)));
                            }
                        }
                        runTimeItemValue.setSortLastAvgValue(Double.valueOf(valueavg)>10000000?10000000:Double.valueOf(valueavg));
                        runTimeItemValue.setAssetName(m.getAssetsName());
                        runTimeItemValue.setAssetsId(m.getId());
                        runTimeItemValue.setIp(m.getInBandIp());
                        runTimeItemValue.setItemName(name);
                        runTimeItemValue.setHostId(m.getAssetsId());
                        runTimeItemValue.setServerId(m.getMonitorServerId());
                        if(StringUtils.isBlank(runTimeItemValue.getMaxValueTime())){
                            runTimeItemValue.setMaxValueTime(format.format(new Date(startTime*1000)));
                        }
                        if(StringUtils.isBlank(runTimeItemValue.getMinValueTime())){
                            runTimeItemValue.setMinValueTime(format.format(new Date(startTime*1000)));
                        }
                        runTimeItemValues.add(runTimeItemValue);
                    }
                }
            }
        }catch (Throwable e){
            log.error("计算数据失败，失败信息:"+e.getMessage());
        }
        return runTimeItemValues;
    }




    private  Map<Integer, List<String>> getFenK(List<String> k, int size) {
        Map<Integer, List<String>> fen = new HashMap<>();
        if (k.size()<size){
            fen.put(0,k);
        }else {
            Integer kill = k.size()/size;
            Integer yushu = k.size()%size;
            for (int i = 0; i < kill; i++) {
                fen.put(i+1,k.subList((i)*size,(i+1)*size));
            }
            if(yushu>0){
                fen.put(kill+1,k.subList(kill*size,kill*size+yushu));
            }
        }
        return fen;
    }

    public List<RunTimeItemValue> getrunTimeCpuAndMemory(Integer userId, Long startTime, Long endTime) {
        log.info("开始查询CPU信息" + new Date());
        MwCommonAssetsDto mwCommonAssetsDto = new MwCommonAssetsDto();
        mwCommonAssetsDto.setUserId(userId);
        Map<String, Object> map = mwAssetsManager.getAssetsByUserId(mwCommonAssetsDto);
        Object assetsList = map.get("assetsList");
        List<RunTimeItemValue> list = new ArrayList<>();
        if (assetsList != null) {
            List<MwTangibleassetsTable> mwTangibleassetsDTOS = (List<MwTangibleassetsTable>) assetsList;
            /**
             * 去除ICMP类型的资产
             */
            if(CollectionUtils.isNotEmpty(mwTangibleassetsDTOS)){
                Iterator<MwTangibleassetsTable> iterator = mwTangibleassetsDTOS.iterator();
                while(iterator.hasNext()){
                    MwTangibleassetsTable next = iterator.next();
                    String assetsTypeName = next.getAssetsTypeName();
                    if(StringUtils.isNotBlank(assetsTypeName) && "ICMP".equals(assetsTypeName)){
                        iterator.remove();
                    }
                }
            }
            if (mwTangibleassetsDTOS.size() == 0) {
                return null;
            }

            //初始化基础数据
            Map<String, RunTimeItemValue> runTimeItemValueMap = new HashMap<>();
            for (MwTangibleassetsTable asset : mwTangibleassetsDTOS) {
                RunTimeItemValue runTimeItemValue = new RunTimeItemValue();
                runTimeItemValue.setAssetName(asset.getAssetsName());
                runTimeItemValue.setAssetsId(asset.getId());
                runTimeItemValue.setIp(asset.getInBandIp());
                runTimeItemValue.setHostId(asset.getAssetsId());
                runTimeItemValue.setServerId(asset.getMonitorServerId());

                String key = BatchGetThreadValue.genRunTimeItemValueKey(runTimeItemValue);
                runTimeItemValueMap.put(key, runTimeItemValue);
            }

            log.info("线程执行获取zabbix数据开始" + new Date());
            List<String> names = new ArrayList<>();
            names.add(RunTimeReportManager.CPU_UTILIZATION);
            names.add(RunTimeReportManager.MEMORY_UTILIZATION);
            names.add(RunTimeReportManager.MEMORY_TOTAL);
            names.add(RunTimeReportManager.MEMORY_USED);
            names.add(RunTimeReportManager.ICMP_RESPONSE_TIME);
            names.add(RunTimeReportManager.MEMORY_FREE);
            BatchGetThreadValue batchGetThreadValue = new BatchGetThreadValue(mwtpServerAPI);
            batchGetThreadValue.setHostGroupSize(hostGroupSize);
            log.info("hostGroupSize:" + hostGroupSize);
            Map<String, RunTimeItemValue> ret = batchGetThreadValue.getThreadValue(runTimeItemValueMap, names, startTime, endTime);

            log.info("线程执行获取zabbix数据结束" + new Date());
            list.addAll(ret.values());
        }
        return list;
    }


    private RunTimeItemValue getThreadValue3(MwTangibleassetsTable asset, String name, Long startTime, Long endTime) {
        RunTimeItemValue runTimeItemValue = new RunTimeItemValue();
        QueryAssetsAvailableParam param = new QueryAssetsAvailableParam();
        param.setMonitorServerId(asset.getMonitorServerId());
        param.setInBandIp(asset.getInBandIp());
        param.setAssetsId(asset.getAssetsId());
        param.setId(asset.getId());
        QueryAssetsAvailableParam newParam = mwServerManager.getItemIdByAvailableItem(param);
        if (newParam.getItemId() != null && StringUtils.isNotEmpty(newParam.getItemId())) {

            Double sum = 0.0;
            List<MWItemHistoryDto> historyDtos = zabbixManger.HistoryGetByTimeAndHistory(newParam.getMonitorServerId(), newParam.getItemId(), startTime, endTime, newParam.getValue_type());
            if (historyDtos != null && historyDtos.size() > 0) {
                for (MWItemHistoryDto historyDto : historyDtos) {

                    if (historyDto.getLastValue() == 1L || historyDto.getLastValue() == 2L) {
                        sum++;
                    }
                }
                int size = historyDtos.size();

                String per = new BigDecimal(sum * 100 / size).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                runTimeItemValue.setAssetUtilization(per);
                runTimeItemValue.setAssetName(asset.getAssetsName());
                runTimeItemValue.setIp(asset.getInBandIp());
//                runTimeItemValue.setSortLastAvgValue((sum * 100 / size));
            }
        }

        return runTimeItemValue;
    }

    private RunTimeItemValue getThreadValue(MwTangibleassetsTable asset, String name, Long startTime, Long endTime) {
        log.info("开始查询zabbix数据" + new Date());
        Integer monitorServerId = asset.getMonitorServerId();
        String hostId = asset.getAssetsId();
        log.info("开始查询zabbix数据Item" + new Date());
        MWZabbixAPIResult result0 = mwtpServerAPI.itemGetbyType(monitorServerId, name, hostId, false);
        log.info("结束查询zabbix数据Item" + new Date());
        Set<String> items = new HashSet<>();
        if (result0.getCode() == 0) {
            JsonNode itemData = (JsonNode) result0.getData();
            if (itemData.size() > 0) {
                List<String> itemIds = new ArrayList<>();
                Integer vlaueType = itemData.get(0).get("value_type").asInt();
                for (int i = 0; i < itemData.size(); i++) {
                    if (itemData.get(i) == null || itemData.get(i).get("itemid") == null) {
                        continue;
                    }
                    String itemid = itemData.get(i).get("itemid").asText();
                    items.add(itemid);
                    itemIds.add(itemid);
                }
                RunTimeItemValue runTimeItemValue = new RunTimeItemValue();
                log.info("开始查询zabbix数据History" + new Date());
                MWZabbixAPIResult historyRsult = mwtpServerAPI.HistoryGetByTimeAndType(monitorServerId, itemIds, startTime, endTime, vlaueType);
                log.info("结束查询zabbix数据History" + new Date());
                List<HistoryValueDto> valueData = ReportUtil.getValueData(historyRsult);
                TrendDto trendDtoNotUnit = ReportUtil.getTrendDtoNotUnit(valueData);
                String values = new BigDecimal(trendDtoNotUnit.getValueAvg()).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                runTimeItemValue.setAvgValue(values + "%");
                runTimeItemValue.setSortLastAvgValue(Double.valueOf(trendDtoNotUnit.getValueAvg()));
                if (CollectionUtils.isEmpty(valueData)) {
                    runTimeItemValue.setMaxValue(values + "%");
                    runTimeItemValue.setMinValue(values + "%");
                } else {
                    runTimeItemValue.setMaxValue(new BigDecimal(trendDtoNotUnit.getValueMax()).setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "%");
                    runTimeItemValue.setMinValue(new BigDecimal(trendDtoNotUnit.getValueMin()).setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "%");
                }
                runTimeItemValue.setAssetName(asset.getAssetsName());
                runTimeItemValue.setAssetsId(asset.getId());
                runTimeItemValue.setIp(asset.getInBandIp());
                runTimeItemValue.setItemIds(items);
                runTimeItemValue.setItemName(name);
                runTimeItemValue.setHostId(asset.getAssetsId());
                runTimeItemValue.setServerId(asset.getMonitorServerId());
                return runTimeItemValue;
            }
        }
        log.info("结束查询zabbix数据" + new Date());
        return null;
    }

    public List<RunTimeItemValue> getrunTimeMemory2(Integer userId, String name, Long startTime, Long endTime) {
        MwCommonAssetsDto mwCommonAssetsDto = new MwCommonAssetsDto();
        mwCommonAssetsDto.setUserId(userId);
        Map<String, Object> map = mwAssetsManager.getAssetsByUserId(mwCommonAssetsDto);
        Object assetsList = map.get("assetsList");
        List<RunTimeItemValue> list = new ArrayList<>();
        if (assetsList != null) {
            List<MwTangibleassetsTable> mwTangibleassetsDTOS = (List<MwTangibleassetsTable>) assetsList;
            if (mwTangibleassetsDTOS.size() == 0) {
                return null;
            }
            CopyOnWriteArrayList<Future<List<RunTimeItemValue>>> futureList = new CopyOnWriteArrayList<>();
            ExecutorService executorService = Executors.newFixedThreadPool(20);
            for (MwTangibleassetsTable mwTangibleassetsDTO : mwTangibleassetsDTOS) {
                GetDataByThread<List<RunTimeItemValue>> getDataByThread = new GetDataByThread<List<RunTimeItemValue>>() {
                    @Override
                    public List<RunTimeItemValue> call() throws Exception {
                        List<RunTimeItemValue> threadValue2 = getThreadValue2(mwTangibleassetsDTO, name, startTime, endTime);
                        return threadValue2;
                    }
                };
                Future<List<RunTimeItemValue>> submit = executorService.submit(getDataByThread);
                futureList.add(submit);
            }
            for (Future<List<RunTimeItemValue>> listFuture : futureList) {
                try {
                    List<RunTimeItemValue> list1 = listFuture.get(30, TimeUnit.SECONDS);
                    list1.forEach(f -> list.add(f));
                } catch (Exception e) {
                    listFuture.cancel(true);
                } finally {
                    executorService.shutdown();
                }
            }

        }
        return list;
    }

    private List<RunTimeItemValue> getThreadValue2(MwTangibleassetsTable asset, String name, Long startTime, Long endTime) {
        List<RunTimeItemValue> list = new ArrayList<>();
//        RunTimeItemValue runTimeItemValue = new RunTimeItemValue();
        Integer monitorServerId = asset.getMonitorServerId();
        String hostId = asset.getAssetsId();
        MWZabbixAPIResult result0 = mwtpServerAPI.itemGetbyType(monitorServerId, name, hostId, false);
        if (result0.getCode() == 0) {
            JsonNode itemData = (JsonNode) result0.getData();
            if (itemData.size() > 0) {
                Integer vlaueType = itemData.get(0).get("value_type").asInt();
                for (int i = 0; i < itemData.size(); i++) {
                    RunTimeItemValue runTimeItemValue = new RunTimeItemValue();
                    String itemid = itemData.get(i).get("itemid").asText();
                    int typeIndex = itemData.get(i).get("name").asText().indexOf("]");
                    String typeName = itemData.get(i).get("name").asText().substring(1, typeIndex);
                    MWZabbixAPIResult historyRsult = mwtpServerAPI.HistoryGetByTimeAndType(monitorServerId, itemid, startTime, endTime, vlaueType);
                    List<HistoryValueDto> valueData = ReportUtil.getValueData(historyRsult);
                    TrendDto trendDtoNotUnit = ReportUtil.getTrendDtoNotUnit1(valueData);
                    String values = new BigDecimal(trendDtoNotUnit.getValueAvg()).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                    runTimeItemValue.setAvgValue(values);
                    runTimeItemValue.setSortLastAvgValue(Double.valueOf(trendDtoNotUnit.getValueAvg()));
//                    runTimeItemValue.setMaxValue(trendDtoNotUnit.getValueMax());
//                    runTimeItemValue.setMinValue(trendDtoNotUnit.getValueMin());
                    runTimeItemValue.setAssetName(asset.getAssetsName());
                    if ("DISK_UTILIZATION".equals(name)) {
                        runTimeItemValue.setDiskName(typeName);
                        runTimeItemValue.setIp(asset.getInBandIp());
                    }
                    if ("INTERFACE_IN_UTILIZATION".equals(name) || "INTERFACE_OUT_UTILIZATION".equals(name)) {
                        MWZabbixAPIResult result1 = mwtpServerAPI.itemGetbyType(monitorServerId, "[" + typeName + "]INTERFACE_OUT_UTILIZATION", hostId, false);
                        if (result1.getCode() == 0) {
                            JsonNode itemData1 = (JsonNode) result1.getData();
                            if (itemData1.size() > 0) {
                                Integer vlaueType1 = itemData1.get(0).get("value_type").asInt();

//                                RunTimeItemValue runTimeItemValue = new RunTimeItemValue();
                                String itemid1 = itemData1.get(0).get("itemid").asText();
//                                int typeIndex1 = itemData.get(0).get("name").asText().indexOf("]");
//                                String typeName1 = itemData.get(0).get("name").asText().substring(1, typeIndex);
                                MWZabbixAPIResult historyRsult1 = mwtpServerAPI.HistoryGetByTimeAndType(monitorServerId, itemid1, startTime, endTime, vlaueType1);
                                List<HistoryValueDto> valueData1 = ReportUtil.getValueData(historyRsult1);
                                TrendDto trendDtoNotUnit1 = ReportUtil.getTrendDtoNotUnit1(valueData1);
                                String values1 = new BigDecimal(trendDtoNotUnit1.getValueAvg()).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                                runTimeItemValue.setOutInterfaceAvgValue(values1);
                            }
                        }
                        runTimeItemValue.setInterfaceName(typeName);
                        runTimeItemValue.setIp(asset.getInBandIp());
                    }
                    list.add(runTimeItemValue);
                }

            }
        }
        return list;
    }


    public PeriodTrendDto getTodayAlertTrend(List<Date> list) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        PeriodTrendDto periodTrendDto = new PeriodTrendDto();
        Date dateStart = list.get(0);
        Date dateEnd = list.get(1);
        Calendar calStart = Calendar.getInstance();
        calStart.setTime(dateStart);
        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(dateEnd);
        int ii = calEnd.get(Calendar.HOUR_OF_DAY);
        List<Integer> listOfCount = new ArrayList<>();
        List<String> listOfTime = new ArrayList<>();
        HashMap<Integer, Integer> countMap = new HashMap<>();
        QueryTangAssetsParam assetsParam = new QueryTangAssetsParam();
        assetsParam.setPageNumber(1);
        assetsParam.setPageSize(Integer.MAX_VALUE);
        assetsParam.setIsQueryAssetsState(false);
        assetsParam.setUserId(mwUserCommonService.getAdmin());
        List<MwTangibleassetsTable> mwTangAssetses = mwAssetsManager.getAssetsTable(assetsParam);
        Map<Integer, List<String>> maps = mwTangAssetses.stream().filter(item -> item.getMonitorServerId() != null && item.getMonitorServerId() != 0)
                .collect(Collectors.groupingBy(MwTangibleassetsTable::getMonitorServerId, Collectors.mapping(MwTangibleassetsTable::getAssetsId, Collectors.toList())));
        for (Integer key : maps.keySet()) {
            List<String> hostIds = maps.get(key);
            if (hostIds.size() > 0) {
                Calendar calendar = Calendar.getInstance();
                for (int i = 0; i <= ii; i++) {
                    Integer count = alertManager.getAlertTodayHistory(key, hostIds, String.valueOf(calEnd.getTimeInMillis()).substring(0, 10));
                    if (null != count) {
                        if (countMap.containsKey(i)) {
                            countMap.put(i, count + countMap.get(i));
                        } else {
                            countMap.put(i, count);
                            listOfTime.add(format.format(calEnd.getTime()).substring(10));
                        }
                    }
                    calEnd.add(Calendar.HOUR_OF_DAY, -1);
                }
            }
        }
        if (countMap.size() > 0) {
            for (Integer i : countMap.keySet()) {
                listOfCount.add(countMap.get(i));

            }
        }
        Collections.reverse(listOfCount);
        Collections.reverse(listOfTime);
        periodTrendDto.setCount(listOfCount);
        periodTrendDto.setDate(listOfTime);
        return periodTrendDto;

    }

    private PeriodTrendDto getLastMonthAlertTrend(List<Date> list) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        PeriodTrendDto periodTrendDto = new PeriodTrendDto();
        List<Integer> listOfCount = new ArrayList<>();
        List<String> listOfTime = new ArrayList<>();
        Date dateStart = list.get(0);
        Date dateEnd = list.get(1);
        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(dateEnd);
        final int days = calEnd.get(Calendar.DAY_OF_MONTH);
        Calendar calStart = Calendar.getInstance();

        calStart.setTime(dateStart);
        //变成23:59:59
        calStart.set(Calendar.HOUR_OF_DAY, 23);
        calStart.set(Calendar.SECOND, 59);
        calStart.set(Calendar.MINUTE, 59);

        HashMap<Integer, Integer> countMap = new HashMap<>();
        QueryTangAssetsParam assetsParam = new QueryTangAssetsParam();
        assetsParam.setPageNumber(1);
        assetsParam.setPageSize(Integer.MAX_VALUE);
        assetsParam.setIsQueryAssetsState(false);
        assetsParam.setUserId(mwUserCommonService.getAdmin());
        List<MwTangibleassetsTable> mwTangAssetses = mwAssetsManager.getAssetsTable(assetsParam);
        Map<Integer, List<String>> maps = mwTangAssetses.stream().filter(item -> item.getMonitorServerId() != null && item.getMonitorServerId() != 0)
                .collect(Collectors.groupingBy(MwTangibleassetsTable::getMonitorServerId, Collectors.mapping(MwTangibleassetsTable::getAssetsId, Collectors.toList())));
        for (Integer key : maps.keySet()) {
            List<String> hostIds = maps.get(key);
            if (hostIds.size() > 0) {
                if (days % 4 != 0) {
                    for (int i = 0; i <= 6; i++) {
                        if (i == 0) {
                            calStart.add(Calendar.DAY_OF_MONTH, 3);
                        } else {
                            calStart.add(Calendar.DAY_OF_MONTH, 4);
                        }
                        Integer count = alertManager.getAlertTodayHistory(key, hostIds, String.valueOf(calStart.getTimeInMillis()).substring(0, 10));
                        if (null != count) {
                            if (countMap.containsKey(i)) {
                                countMap.put(i, count + countMap.get(i));
                            } else {
                                countMap.put(i, count);
                                listOfTime.add(format.format(calStart.getTime()));
                            }
                        }
                    }
                    //这个月最后一天时间数据
                    Integer count = alertManager.getAlertTodayHistory(key, hostIds, String.valueOf(calEnd.getTimeInMillis()).substring(0, 10));
                    if (null != count) {
                        if (countMap.containsKey(7)) {
                            countMap.put(7, count + countMap.get(7));
                        } else {
                            countMap.put(7, count);
                            listOfTime.add(format.format(calEnd.getTime()));
                        }
                    }
                } else {
                    for (int i = 0; i <= 6; i++) {
                        if (i == 0) {
                            calStart.add(Calendar.DAY_OF_MONTH, 3);
                        } else {
                            calStart.add(Calendar.DAY_OF_MONTH, 4);
                        }
                        Integer count = alertManager.getAlertTodayHistory(key, hostIds, String.valueOf(calStart.getTimeInMillis()).substring(0, 10));
                        if (null != count) {
                            if (countMap.containsKey(i)) {
                                countMap.put(i, count + countMap.get(i));
                            } else {
                                countMap.put(i, count);
                                listOfTime.add(format.format(calStart.getTime()));
                            }
                        }
                    }
                }

            }
        }
        if (countMap.size() > 0) {
            for (Integer i : countMap.keySet()) {
                listOfCount.add(countMap.get(i));
            }
        }
        periodTrendDto.setCount(listOfCount);
        periodTrendDto.setDate(listOfTime);
        return periodTrendDto;
    }

    public PeriodTrendDto getLastWeekAlertTrend(List<Date> list) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        PeriodTrendDto periodTrendDto = new PeriodTrendDto();
        Date dateStart = list.get(0);
        Date dateEnd = list.get(1);
        Integer dete = Math.toIntExact(((dateEnd.getTime() - dateStart.getTime()) / 86400000 ));
        Calendar calStart = Calendar.getInstance();
        calStart.setTime(dateStart);
        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(dateEnd);
        List<Integer> listOfCount = new ArrayList<>();
        List<String> listOfTime = new ArrayList<>();
        HashMap<Integer, Integer> countMap = new HashMap<>();
        QueryTangAssetsParam assetsParam = new QueryTangAssetsParam();
        assetsParam.setPageNumber(1);
        assetsParam.setPageSize(Integer.MAX_VALUE);
        assetsParam.setIsQueryAssetsState(false);
        assetsParam.setUserId(mwUserCommonService.getAdmin());
        List<MwTangibleassetsTable> mwTangAssetses = mwAssetsManager.getAssetsTable(assetsParam);
        Map<Integer, List<String>> maps = mwTangAssetses.stream().filter(item -> item.getMonitorServerId() != null && item.getMonitorServerId() != 0)
                .collect(Collectors.groupingBy(MwTangibleassetsTable::getMonitorServerId, Collectors.mapping(MwTangibleassetsTable::getAssetsId, Collectors.toList())));
        for (Integer key : maps.keySet()) {
            List<String> hostIds = maps.get(key);
            if (hostIds.size() > 0) {
                for (int i = 0; i <= dete; i++) {
                    Integer count = alertManager.getAlertTodayHistory(key, hostIds, String.valueOf(calEnd.getTimeInMillis()).substring(0, 10));
                    if (null != count) {
                        if (countMap.containsKey(i)) {
                            countMap.put(i, count + countMap.get(i));
                        } else {
                            countMap.put(i, count);
                            listOfTime.add(format.format(calEnd.getTime()));
                        }
                    }
                    calEnd.add(Calendar.DAY_OF_MONTH, -1);
                }
            }
        }
        if (countMap.size() > 0) {
            for (Integer i : countMap.keySet()) {
                listOfCount.add(countMap.get(i));

            }
        }
        Collections.reverse(listOfCount);
        Collections.reverse(listOfTime);
        periodTrendDto.setCount(listOfCount);
        periodTrendDto.setDate(listOfTime);
        return periodTrendDto;

    }




    public PeriodTrendDto getAlertTrendDto(RunTimeQueryParam param) {
        List<Date> dateByType = getDateByType(param);
        DateTimeTypeEnum typeEnum = DateTimeTypeEnum.getByValue(param.getDateType());
        switch (typeEnum) {
            case TODAY:
                return getTodayAlertTrend(dateByType);
            case YESTERDAY:
                return getTodayAlertTrend(dateByType);
            case LAST_WEEK:
                return getLastWeekAlertTrend(dateByType);
            case LAST_MONTH:
                return getLastMonthAlertTrend(dateByType);
            case USER_DEFINED:
                if (dateByType.get(1).getTime()-dateByType.get(0).getTime()>604800000-1000){
                    return getLastWeekAlertTrend(dateByType);
                }
                if (dateByType.get(1).getTime()-dateByType.get(0).getTime()>86400000-1000){
                    return getLastWeekAlertTrend(dateByType);
                }
                else {
                    return getTodayAlertTrend(dateByType);
                }
            default:
                return null;
        }
    }

    public PeriodTrendDto getAssetPeriodTrendDto(RunTimeQueryParam param) {
        List<Date> dateByType = getDateByType(param);
        DateTimeTypeEnum typeEnum = DateTimeTypeEnum.getByValue(param.getDateType());
        switch (typeEnum) {
            case TODAY:
                return getTodayAssetTrend(dateByType);
            case YESTERDAY:
                return getTodayAssetTrend(dateByType);
            case LAST_WEEK:
                return getLastWeekAssetTrend(dateByType);
            case LAST_MONTH:
                return getLastMonthAssetTrend(dateByType);
            case USER_DEFINED:
                if (dateByType.get(1).getTime()-dateByType.get(0).getTime()>604800000-1000){
                    return   getLastWeekAssetTrend(dateByType);
                }
                if (dateByType.get(1).getTime()-dateByType.get(0).getTime()>86400000-1000){
                    return getLastWeekAssetTrend(dateByType);
                }else {
                    return getTodayAssetTrend(dateByType);
                }
            default:
                return null;
        }
    }


    public PeriodTrendDto getAssetUnNormalPeriodTrendDto(RunTimeQueryParam param) {
        List<Date> dateByType = getDateByType(param);
        DateTimeTypeEnum typeEnum = DateTimeTypeEnum.getByValue(param.getDateType());
        switch (typeEnum) {
            case TODAY:
                return getTodayAssetUnNormalTrend(dateByType);
            case YESTERDAY:
                return getTodayAssetUnNormalTrend(dateByType);
            case LAST_WEEK:
                return getLastWeekAssetUnNormalTrend(dateByType);
            case LAST_MONTH:
                return getLastMonthAssetUnNormalTrend(dateByType);
            case USER_DEFINED:
                if (dateByType.get(1).getTime()-dateByType.get(0).getTime()>604800000-1000){
                    return getLastWeekAssetUnNormalTrend(dateByType);
                }
                if (dateByType.get(1).getTime()-dateByType.get(0).getTime()>86400000-1000){
                    return getLastWeekAssetUnNormalTrend(dateByType);
                }else {
                    return getTodayAssetUnNormalTrend(dateByType);
                }
            default:
                return null;
        }
    }


    private PeriodTrendDto getLastMonthAssetUnNormalTrend(List<Date> list) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        PeriodTrendDto periodTrendDto = new PeriodTrendDto();
        List<Integer> listOfCount = new ArrayList<>();
        List<String> listOfTime = new ArrayList<>();
        Date dateStart = list.get(0);
        Date dateEnd = list.get(1);
        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(dateEnd);
        final int days = calEnd.get(Calendar.DAY_OF_MONTH);
        Calendar calStart = Calendar.getInstance();

        calStart.setTime(dateStart);
        //变成23:59:59
        calStart.set(Calendar.HOUR_OF_DAY, 23);
        calStart.set(Calendar.SECOND, 59);
        calStart.set(Calendar.MINUTE, 59);

        calStart.add(Calendar.DAY_OF_MONTH, 3);
        Integer userId = iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId();
        QueryTangAssetsParam assetsParam = new QueryTangAssetsParam();
        assetsParam.setPageNumber(1);
        assetsParam.setPageSize(Integer.MAX_VALUE);
        assetsParam.setIsQueryAssetsState(false);
        assetsParam.setUserId(mwUserCommonService.getAdmin());
        List<MwTangibleassetsTable> mwTangAssetses = mwAssetsManager.getAssetsTable(assetsParam);
        List<AssetByTypeDto> dtoList1 = new ArrayList<>();
        for (MwTangibleassetsTable mwTangAssets : mwTangAssetses) {
            AssetByTypeDto byTypeDto = new AssetByTypeDto();
            byTypeDto.extractFrom(mwTangAssets);
            dtoList1.add(byTypeDto);
        }
        int unNormalAssetTrend1 = getUnNormalAssetTrend(dtoList1);
        listOfCount.add(unNormalAssetTrend1);
        listOfTime.add(format.format(calStart.getTime()));
        if (days % 4 != 0) {
            for (int i = 0; i < 6; i++) {
                calStart.add(Calendar.DAY_OF_MONTH, 4);
                Date time = calStart.getTime();
                List<AssetByTypeDto> dtoList = dtoList1;
                int unNormalAssetTrend = getUnNormalAssetTrend(dtoList);
                listOfCount.add(unNormalAssetTrend);
                listOfTime.add(format.format(calStart.getTime()));
            }
            List<AssetByTypeDto> dtoList = dtoList1;
            int unNormalAssetTrend = getUnNormalAssetTrend(dtoList);
            int size = dtoList.size();
            listOfCount.add(unNormalAssetTrend);
            listOfTime.add(format.format(calEnd.getTime()));

        } else {
            for (int i = 0; i < 6; i++) {
                calStart.add(Calendar.DAY_OF_MONTH, 4);
                Date time = calStart.getTime();
                List<AssetByTypeDto> dtoList = mwReportDao.selectAeestInfo(null, time);
                int unNormalAssetTrend = getUnNormalAssetTrend(dtoList);
                listOfCount.add(unNormalAssetTrend);
                listOfTime.add(format.format(calStart.getTime()));
            }
        }
        periodTrendDto.setCount(listOfCount);
        periodTrendDto.setDate(listOfTime);
        return periodTrendDto;
    }

    private PeriodTrendDto getLastMonthAssetTrend(List<Date> list) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        PeriodTrendDto periodTrendDto = new PeriodTrendDto();
        List<Integer> listOfCount = new ArrayList<>();
        List<String> listOfTime = new ArrayList<>();
        Date dateStart = list.get(0);
        Date dateEnd = list.get(1);
        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(dateEnd);
        final int days = calEnd.get(Calendar.DAY_OF_MONTH);
        Calendar calStart = Calendar.getInstance();

        calStart.setTime(dateStart);
        //变成23:59:59
        calStart.set(Calendar.HOUR_OF_DAY, 23);
        calStart.set(Calendar.SECOND, 59);
        calStart.set(Calendar.MINUTE, 59);

        calStart.add(Calendar.DAY_OF_MONTH, 3);
        QueryTangAssetsParam assetsParam = new QueryTangAssetsParam();
        assetsParam.setPageNumber(1);
        assetsParam.setPageSize(Integer.MAX_VALUE);
        assetsParam.setIsQueryAssetsState(false);
        assetsParam.setUserId(mwUserCommonService.getAdmin());
        List<MwTangibleassetsTable> mwTangAssetses = mwAssetsManager.getAssetsTable(assetsParam);
        List<AssetByTypeDto> dtoList1 = new ArrayList<>();
        for (MwTangibleassetsTable mwTangAssets : mwTangAssetses) {
            AssetByTypeDto byTypeDto = new AssetByTypeDto();
            byTypeDto.extractFrom(mwTangAssets);
            dtoList1.add(byTypeDto);
        }
        int size1 = dtoList1.size();
        listOfCount.add(size1);
        listOfTime.add(format.format(calStart.getTime()));
        if (days % 4 != 0) {
            for (int i = 0; i < 6; i++) {
                calStart.add(Calendar.DAY_OF_MONTH, 4);
                Date time = calStart.getTime();
                List<AssetByTypeDto> dtoList = mwReportDao.selectAeestInfo(null, time);
                int size = dtoList.size();
                listOfCount.add(size);
                listOfTime.add(format.format(calStart.getTime()));
            }
            List<AssetByTypeDto> dtoList = dtoList1;
            int size = dtoList.size();
            listOfCount.add(size);
            listOfTime.add(format.format(calEnd.getTime()));

        } else {
            for (int i = 0; i < 6; i++) {
                calStart.add(Calendar.DAY_OF_MONTH, 4);
                Date time = calStart.getTime();
                List<AssetByTypeDto> dtoList = mwReportDao.selectAeestInfo(null, time);
                int size = dtoList.size();
                listOfCount.add(size);
                listOfTime.add(format.format(calStart.getTime()));
            }
        }
        periodTrendDto.setCount(listOfCount);
        periodTrendDto.setDate(listOfTime);
        return periodTrendDto;
    }

    private PeriodTrendDto getLastWeekAssetTrend(List<Date> list) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        PeriodTrendDto periodTrendDto = new PeriodTrendDto();
        List<Integer> listOfCount = new ArrayList<>();
        List<String> listOfTime = new ArrayList<>();
        Date dateEnd = list.get(1);
        Date dateStart = list.get(0);
        Integer dete = Math.toIntExact(((dateEnd.getTime() - dateStart.getTime()) / 86400000 ));
        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(dateEnd);
        QueryTangAssetsParam assetsParam = new QueryTangAssetsParam();
        assetsParam.setPageNumber(1);
        assetsParam.setPageSize(Integer.MAX_VALUE);
        assetsParam.setIsQueryAssetsState(false);
        assetsParam.setUserId(mwUserCommonService.getAdmin());
        List<MwTangibleassetsTable> mwTangAssetses = mwAssetsManager.getAssetsTable(assetsParam);
        List<AssetByTypeDto> dtoList1 = new ArrayList<>();
        for (MwTangibleassetsTable mwTangAssets : mwTangAssetses) {
            AssetByTypeDto byTypeDto = new AssetByTypeDto();
            byTypeDto.extractFrom(mwTangAssets);
            dtoList1.add(byTypeDto);
        }
        String substring1 = format.format(calEnd.getTime());
        listOfTime.add(substring1);
        int size1 = dtoList1.size();
        listOfCount.add(size1);
        for (int i = 0; i < dete; i++) {
            calEnd.add(Calendar.DAY_OF_MONTH, -1);
            Date time = calEnd.getTime();
            List<AssetByTypeDto> dtoList = dtoList1;
            int size = dtoList.size();
            listOfCount.add(size);
            listOfTime.add(format.format(calEnd.getTime()));
        }
        Collections.reverse(listOfCount);
        Collections.reverse(listOfTime);
        periodTrendDto.setCount(listOfCount);
        periodTrendDto.setDate(listOfTime);
        return periodTrendDto;
    }

    private PeriodTrendDto getTodayAssetTrend(List<Date> list) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        PeriodTrendDto periodTrendDto = new PeriodTrendDto();
        List<Integer> listOfCount = new ArrayList<>();
        List<String> listOfTime = new ArrayList<>();
        Date dateStart = list.get(0);
        Date dateEnd = list.get(1);
        Calendar calStart = Calendar.getInstance();
        calStart.setTime(dateStart);
        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(dateEnd);
        final int h = calEnd.get(Calendar.HOUR_OF_DAY);
        QueryTangAssetsParam assetsParam = new QueryTangAssetsParam();
        assetsParam.setPageNumber(1);
        assetsParam.setPageSize(Integer.MAX_VALUE);
        assetsParam.setIsQueryAssetsState(false);
        assetsParam.setUserId(mwUserCommonService.getAdmin());
        List<MwTangibleassetsTable> mwTangAssetses = mwAssetsManager.getAssetsTable(assetsParam);
        List<AssetByTypeDto> dtoList1 = new ArrayList<>();
        for (MwTangibleassetsTable mwTangAssets : mwTangAssetses) {
            AssetByTypeDto byTypeDto = new AssetByTypeDto();
            byTypeDto.extractFrom(mwTangAssets);
            dtoList1.add(byTypeDto);
        }
        String substring1 = format.format(calEnd.getTime()).substring(10);
        listOfTime.add(substring1);
        int size1 = dtoList1.size();
        listOfCount.add(size1);
        for (int i = 0; i < h; i++) {
            calEnd.add(Calendar.HOUR_OF_DAY, -1);
            Date time = calEnd.getTime();
            List<AssetByTypeDto> dtoList = dtoList1;
            int size = dtoList.size();
            listOfCount.add(size);
            listOfTime.add(format.format(calEnd.getTime()).substring(10));
        }
        Collections.reverse(listOfCount);
        Collections.reverse(listOfTime);
        periodTrendDto.setCount(listOfCount);
        periodTrendDto.setDate(listOfTime);
        return periodTrendDto;
    }

    private PeriodTrendDto getLastWeekAssetUnNormalTrend(List<Date> list) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        PeriodTrendDto periodTrendDto = new PeriodTrendDto();
        List<Integer> listOfCount = new ArrayList<>();
        List<String> listOfTime = new ArrayList<>();
        Date dateEnd = list.get(1);
        Date dateStart = list.get(0);
        Integer dete =Math.toIntExact(((dateEnd.getTime() - dateStart.getTime()) / 86400000 ));
        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(dateEnd);
        QueryTangAssetsParam assetsParam = new QueryTangAssetsParam();
        assetsParam.setPageNumber(1);
        assetsParam.setPageSize(Integer.MAX_VALUE);
        assetsParam.setIsQueryAssetsState(false);
        assetsParam.setUserId(mwUserCommonService.getAdmin());
        List<MwTangibleassetsTable> mwTangAssetses = mwAssetsManager.getAssetsTable(assetsParam);
        List<AssetByTypeDto> dtoList1 = new ArrayList<>();
        for (MwTangibleassetsTable mwTangAssets : mwTangAssetses) {
            AssetByTypeDto byTypeDto = new AssetByTypeDto();
            byTypeDto.extractFrom(mwTangAssets);
            dtoList1.add(byTypeDto);
        }
        int unNormalAssetTrend1 = getUnNormalAssetTrend(dtoList1);
        String substring1 = format.format(calEnd.getTime());
        listOfTime.add(substring1);
        listOfCount.add(unNormalAssetTrend1);
        for (int i = 0; i < dete; i++) {
            calEnd.add(Calendar.DAY_OF_MONTH, -1);
            Date time = calEnd.getTime();
            List<AssetByTypeDto> dtoList = dtoList1;
            int unNormalAssetTrend = getUnNormalAssetTrend(dtoList);
            listOfCount.add(unNormalAssetTrend);
            listOfTime.add(format.format(calEnd.getTime()));
        }
        Collections.reverse(listOfCount);
        Collections.reverse(listOfTime);
        periodTrendDto.setCount(listOfCount);
        periodTrendDto.setDate(listOfTime);
        return periodTrendDto;
    }

    private PeriodTrendDto getTodayAssetUnNormalTrend(List<Date> list) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        PeriodTrendDto periodTrendDto = new PeriodTrendDto();
        List<Integer> listOfCount = new ArrayList<>();
        List<String> listOfTime = new ArrayList<>();
        Date dateStart = list.get(0);
        Date dateEnd = list.get(1);
        Calendar.getInstance().setTime(dateStart);
        Calendar calStart = Calendar.getInstance();
        calStart.setTime(dateStart);
        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(dateEnd);
        final int h = calEnd.get(Calendar.HOUR_OF_DAY);
        QueryTangAssetsParam assetsParam = new QueryTangAssetsParam();
        assetsParam.setPageNumber(1);
        assetsParam.setPageSize(Integer.MAX_VALUE);
        assetsParam.setIsQueryAssetsState(false);
        assetsParam.setUserId(mwUserCommonService.getAdmin());
        List<MwTangibleassetsTable> mwTangAssetses = mwAssetsManager.getAssetsTable(assetsParam);
        List<AssetByTypeDto> dtoList1 = new ArrayList<>();
        for (MwTangibleassetsTable mwTangAssets : mwTangAssetses) {
            AssetByTypeDto byTypeDto = new AssetByTypeDto();
            byTypeDto.extractFrom(mwTangAssets);
            dtoList1.add(byTypeDto);
        }
        int unNormalAssetTrend1 = getUnNormalAssetTrend(dtoList1);
        String substring1 = format.format(calEnd.getTime()).substring(10);
        listOfTime.add(substring1);
        listOfCount.add(unNormalAssetTrend1);
        for (int i = 0; i < h; i++) {
            calEnd.add(Calendar.HOUR_OF_DAY, -1);
            Date time = calEnd.getTime();
            List<AssetByTypeDto> dtoList = dtoList1;
            int unNormalAssetTrend = getUnNormalAssetTrend(dtoList);
            listOfCount.add(unNormalAssetTrend);
            listOfTime.add(format.format(calEnd.getTime()).substring(10));
        }
        Collections.reverse(listOfCount);
        Collections.reverse(listOfTime);
        periodTrendDto.setCount(listOfCount);
        periodTrendDto.setDate(listOfTime);
        return periodTrendDto;
    }

    public int getUnNormalAssetTrend(List<AssetByTypeDto> assetByTypeDtos) {
        int abnormal = 0;
        //加资产健康状态
        if (assetByTypeDtos != null && assetByTypeDtos.size() > 0) {
            Map<Integer, List<String>> groupMap = assetByTypeDtos.stream().filter(item->item.getMonitorServerId() != null)
                    .collect(Collectors.groupingBy(AssetByTypeDto::getMonitorServerId, Collectors.mapping(AssetByTypeDto::getAssetsId, Collectors.toList())));
            Map<String, String> statusMap = new HashMap<>();
            for (Map.Entry<Integer, List<String>> value : groupMap.entrySet()) {
                if (value.getKey() != null && value.getKey() > 0) {
                    //有改动-zabbi
                    MWZabbixAPIResult statusData = mwtpServerAPI.itemGetbySearch(value.getKey(), ZabbixItemConstant.ASSETS_STATUS, value.getValue());
                    if (!statusData.isFail()) {
                        JsonNode jsonNode = (JsonNode) statusData.getData();
                        if (jsonNode.size() > 0) {
                            for (JsonNode node : jsonNode) {
                                int itemid = node.get("itemid").asInt();
//                                MWZabbixAPIResult historyRsult = mwtpServerAPI.HistoryGetByTimeAndType(value.getKey(), itemid, startTime, endTime, vlaueType);

                                Integer lastvalue = node.get("lastvalue").asInt();
                                String hostId = node.get("hostid").asText();
                                String status = (lastvalue == 0) ? "ABNORMAL" : "NORMAL";
                                statusMap.put(value.getKey() + ":" + hostId, status);
                            }
                        }
                    }
                   /* statusMap.put(value.getKey() + ":" + value.getValue(), "ABNORMAL");*/
                }
            }
            String status = "";
            for (AssetByTypeDto asset : assetByTypeDtos) {
                String s = statusMap.get(asset.getMonitorServerId() + ":" + asset.getAssetsId());
                if (s != null && StringUtils.isNotEmpty(s)) {
                    status = s;
                } else {
                    status = "UNKNOWN";
                }
                asset.setItemAssetsStatus(status);
            }
            Map<String, List<AssetByTypeDto>> collect = assetByTypeDtos.stream().collect(Collectors.groupingBy(AssetByTypeDto::getItemAssetsStatus));
            if (collect != null && collect.containsKey("ABNORMAL")) {
                abnormal = collect.get("ABNORMAL").size();
            }
        }

        return abnormal;
    }

    public List<Date> getDateByType(RunTimeQueryParam param) {
        Integer dateType = param.getDateType();
        DateTimeTypeEnum typeEnum = DateTimeTypeEnum.getByValue(dateType);
        switch (typeEnum) {
            case TODAY:
                return ReportDateUtil.getToday();
            case YESTERDAY:
                return ReportDateUtil.getYesterday();
            case LAST_WEEK:
                return ReportDateUtil.getLastWeek();
            case LAST_MONTH:
                return ReportDateUtil.getLastMonth();
            case CURRENT_QUARTER:
                return ReportDateUtil.getQuarter();
            case CURRENT_YEAR:
                return ReportDateUtil.getYear();
            case USER_DEFINED:
                List<String> chooseTime = param.getChooseTime();
                Date startDate = ReportDateUtil.getDate(chooseTime.get(0) + " " + MWDateConstant.BEGIN_TIME, MWDateConstant.NORM_DATETIME);
                Date endDate = ReportDateUtil.getDate(chooseTime.get(1) + " " + MWDateConstant.END_TIME, MWDateConstant.NORM_DATETIME);
                List<Date> list = new ArrayList<>();
                list.add(startDate);
                list.add(endDate);
                return list;
            default:
                break;
        }
        return null;
    }

    public List<Long> getLongTimeByType(RunTimeQueryParam param) {
        Integer dateType = param.getDateType();
        List<Long> longList = new ArrayList<>();
        DateTimeTypeEnum typeEnum = DateTimeTypeEnum.getByValue(dateType);
        switch (typeEnum) {
            case TODAY:
                List<Date> today = ReportDateUtil.getToday();
                longList.add(today.get(0).getTime() / 1000L);
                longList.add(today.get(1).getTime() / 1000L);
                break;
            case YESTERDAY:
                List<Date> yesterday = ReportDateUtil.getYesterday();
                longList.add(yesterday.get(0).getTime() / 1000L);
                longList.add(yesterday.get(1).getTime() / 1000L);
                break;
            case LAST_WEEK:
                List<Date> lastWeek = ReportDateUtil.getLastWeek();
                longList.add(lastWeek.get(0).getTime() / 1000L);
                longList.add(lastWeek.get(1).getTime() / 1000L);
                break;
            case LAST_MONTH:
                List<Date> lastMonth = ReportDateUtil.getLastMonth();
                longList.add(lastMonth.get(0).getTime() / 1000L);
                longList.add(lastMonth.get(1).getTime() / 1000L);
                break;
            case CURRENT_QUARTER:
                List<Date> quarter = ReportDateUtil.getQuarter();
                longList.add(quarter.get(0).getTime() / 1000L);
                longList.add(quarter.get(1).getTime() / 1000L);
                break;
            case CURRENT_YEAR:
                List<Date> year = ReportDateUtil.getYear();
                longList.add(year.get(0).getTime() / 1000L);
                longList.add(year.get(1).getTime() / 1000L);
            case USER_DEFINED:
                List<String> chooseTime = param.getChooseTime();
                Long startTime = MWUtils.getDate(chooseTime.get(0) + " " + MWDateConstant.BEGIN_TIME, MWDateConstant.NORM_DATETIME);
                Long endTime = MWUtils.getDate(chooseTime.get(1) + " " + MWDateConstant.END_TIME, MWDateConstant.NORM_DATETIME);
                longList.add(startTime);
                longList.add(endTime);
                break;
            default:
                break;
        }
        return longList;
    }


    public Map<String,List<RunTimeItemValue>> getrunTimeMemory(Integer userId, Long startTime, Long endTime, RunTimeQueryParam param) {
        Map<String, List<RunTimeItemValue>> topNMap = new HashMap<>();
        MwCommonAssetsDto mwCommonAssetsDto = new MwCommonAssetsDto();
        //全资产查询
        Integer id = mwUserCommonService.getAdmin();
        mwCommonAssetsDto.setUserId(id);
        Integer adminId = id;
        Map<String, Object> map = mwAssetsManager.getAssetsByUserId(mwCommonAssetsDto);
        //过滤资产查询
//        mwCommonAssetsDto.setUserId(userId);
//        Map<String, Object> maps = mwAssetsManager.getAssetsByUserId(mwCommonAssetsDto);
//        Object assetsListb = maps.get("assetsList");
//        List<MwTangibleassetsTable> mwTangibleassetsDTOSb = new ArrayList<>();
//        if (assetsListb != null) {
//            mwTangibleassetsDTOSb = (List<MwTangibleassetsTable>) assetsListb;
//            if (mwTangibleassetsDTOSb.size() == 0) {
//                return null;
//            }
//        }


        Object assetsList = map.get("assetsList");
        List<RunTimeItemValue> res = new ArrayList<>();
        if (assetsList != null) {
            List<MwTangibleassetsTable> mwTangibleassetsDTOS = (List<MwTangibleassetsTable>) assetsList;
            if (mwTangibleassetsDTOS.size() == 0) {
                return null;
            }
            List<String> strings = new ArrayList<>();
            strings.add("CPU_UTILIZATION");
            strings.add("MEMORY_UTILIZATION");
            strings.add("ICMP_LOSS");
            for (String s : strings) {
                List<RunTimeItemValue> list = new ArrayList<>();
                if (param.getDateType()!=DateTimeTypeEnum.TODAY.getCode()&&!param.getTimingType()){
//                    mwReportDao.selectRuntimeByTypeAndName(s,param.getDateType())
                    list = getOldData(s,new Date(startTime*1000l),new Date(endTime*1000l));

//                    list= getrunTimeMemoryBySql(s,param.getDateType());
                }else {
                    list = runTimeReportManager.getrunTimeMemory(adminId, s, startTime, endTime, param.getDateType());
                }
                if(null!=list&&list.size()>0){
                    list= list.stream().filter(f -> StringUtils.isNotEmpty(f.getAssetName())).collect(Collectors.toList());
                    Collections.sort(list,new RunTimeItemValue());
                    if(list.size()>param.getDataSize()){
                        list=list.subList(0,param.getDataSize());
                    }
                }
                topNMap.put(s,list);
            }
        }
        return topNMap;
    }

    private List<RunTimeItemValue> getOldData(String s, Date startTime, Date endTime) {
//        Integer count = Math.toIntExact((endTime.getTime() - startTime.getTime()) / 86400000l);
        List<RunTimeItemValue> runTimeItemValues = terraceManageDao.selectRunStateNameDailyData(s,startTime,endTime);
        List<RunTimeItemValue> runTimeItemValueList = new ArrayList<>();
        for (int i = 0; i <runTimeItemValues.size() ; i++) {
            Boolean add = true ;
            for (int j = 0; j <runTimeItemValueList.size() ; j++) {
                if (runTimeItemValues.get(i).getAssetsId().equals(runTimeItemValueList.get(j).getAssetsId())){
                    try {
                        String avg =String.valueOf(new BigDecimal((Double.parseDouble(runTimeItemValueList.get(j).getAvgValue())+Double.parseDouble(runTimeItemValues.get(i).getAvgValue()))/2).setScale(2,BigDecimal.ROUND_HALF_UP));
                        runTimeItemValueList.get(j).setAvgValue(avg);
                    }catch (Exception e){

                    }
                    add = false ;
                }
            }
            if (add){
                runTimeItemValueList.add(runTimeItemValues.get(i));
            }
        }
//        List<RunTimeItemValue> all =   mwReportDao.getAll(s,startTime,endTime);


        return runTimeItemValueList;
    }


    private List<RunTimeItemValue> getThreadValue(List<MwTangibleassetsTable> mwTangibleassetsDTOS, Long startTime, Long endTime, Integer type) {
        log.info("topN类型为0,开始执行zabbix查询操作" + new Date());
        RedisUtils redisUtils = SpringUtils.getBean(RedisUtils.class);
        List<RunTimeItemValue> list = new ArrayList<>();
        //根据资产服务ID分组
        Map<Integer, List<String>> assetsMap = new HashMap<>();

        //缓存是否存在今天数据
        boolean changeTime = false;

        Map<String, MwTangibleassetsTable> dtoMap = new HashMap<>();
        for (MwTangibleassetsTable mwTangibleassetsDTO : mwTangibleassetsDTOS) {
            Integer monitorServerId = mwTangibleassetsDTO.getMonitorServerId();
            String assetsId = mwTangibleassetsDTO.getAssetsId();
            dtoMap.put(assetsId, mwTangibleassetsDTO);
            if (assetsMap.isEmpty() || assetsMap.get(monitorServerId) == null) {
                List<String> assetsIds = new ArrayList<>();
                assetsIds.add(assetsId);
                assetsMap.put(monitorServerId, assetsIds);
                continue;
            }
            if (!assetsMap.isEmpty() && assetsMap.get(monitorServerId) != null) {
                List<String> assetsIds = assetsMap.get(monitorServerId);
                assetsIds.add(assetsId);
                assetsMap.put(monitorServerId, assetsIds);
            }
        }
        if (assetsMap.isEmpty()) {
            return null;
        }
        log.info("topN类型0开始查询zabbix" + new Date());
        for (Map.Entry<Integer, List<String>> integerListEntry : assetsMap.entrySet()) {
            Integer monitorServerId = integerListEntry.getKey();
            List<String> hostIds = integerListEntry.getValue();
            List<String> names = new ArrayList<>();
            names.add("CPU_UTILIZATION");
            names.add("MEMORY_UTILIZATION");
            names.add("ICMP_LOSS");


            for (String name : names) {
                log.info("名称" + name + "开始查询zabbix" + new Date());
                long getZabbixStart = System.currentTimeMillis();
                MWZabbixAPIResult result0 = mwtpServerAPI.itemGetbyType(monitorServerId, name, hostIds, false);
                long getZabbixEnd = System.currentTimeMillis();
                log.info("名称" + name + "开始查询zabbix结束" + new Date());
                log.info("执行mwtpServerAPI.itemGetbyType方法,hostIds长度" + hostIds.size() + "查询名称" + name + "耗时" + (getZabbixEnd - getZabbixStart));
                if (result0.getCode() == 0) {
                    JsonNode itemData = (JsonNode) result0.getData();
                    if (itemData != null && itemData.size() > 0) {
                        List<String> itemIds = new ArrayList<>();
                        List<String> hostItemIds = new ArrayList<>();
                        List<String> assetsIds = new ArrayList<>();
                        Integer vlaueType = itemData.get(0).get("value_type").asInt();
                        for (int i = 0; i < itemData.size(); i++) {
                            String itemid = itemData.get(i).get("itemid").asText();
                            if (!changeTime && type == 0) {
                                Map<String, Double> map = (Map<String, Double>) redisUtils.get(name + itemid);
                                if (map != null) {
                                    for (MwTangibleassetsTable mwTangibleassetsDTO : mwTangibleassetsDTOS) {
                                        String assetsId = mwTangibleassetsDTO.getAssetsId();
                                        if (assetsId.equals(itemData.get(i).get("hostid").asText())) {
                                            RunTimeItemValue runTimeItemValue = new RunTimeItemValue();
//                                            List<HistoryValueDto> valueData = ReportUtil.getValueData(historyRsult);
//                                            TrendDto trendDtoNotUnit = ReportUtil.getTrendDtoNotUnit1(valueData);
//                                            String values = new BigDecimal(trendDtoNotUnit.getValueAvg()).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                                            runTimeItemValue.setItemName(name);
                                            runTimeItemValue.setItemId(itemid);
//                                            runTimeItemValue.setAvgValue(values);
//                                            runTimeItemValue.setSortLastAvgValue(Double.valueOf(trendDtoNotUnit.getValueAvg()));
                                            runTimeItemValue.setAssetName(mwTangibleassetsDTO.getAssetsName());
                                            runTimeItemValue.setServerId(mwTangibleassetsDTO.getMonitorServerId());
                                            runTimeItemValue.setIp(mwTangibleassetsDTO.getInBandIp());
                                            list.add(runTimeItemValue);
                                        }
                                    }

                                    Long longTime = Long.valueOf(new BigDecimal(map.get("getTime").toString()).toPlainString());
                                    if (longTime > startTime && longTime < endTime) {
                                        changeTime = true;
                                        startTime = Long.valueOf(longTime.toString());
                                    }
                                }
                            }
                            String hostid = itemData.get(i).get("hostid").asText();
                            hostItemIds.add(hostid + itemid);
                            assetsIds.add(hostid);
                            itemIds.add(itemid);
                        }
                        log.info("名称" + name + "开始查询zabbix" + new Date());
                        getZabbixStart = System.currentTimeMillis();
                        MWZabbixAPIResult historyRsult = mwtpServerAPI.HistoryGetByTimeAndType(monitorServerId, itemIds, startTime, endTime, vlaueType);
                        getZabbixEnd = System.currentTimeMillis();
                        log.info("名称" + name + "开始查询zabbix结束" + new Date());
                        log.info("执行mwtpServerAPI.HistoryGetByTimeAndType方法,itemIds长度" + itemIds.size() + "查询名称" + name + "耗时" + (getZabbixEnd - getZabbixStart));
                        if (historyRsult.getCode() == 0) {
                            JsonNode itemData2 = (JsonNode) historyRsult.getData();
                            Map<String, List<Double>> avgValueMap = new HashMap<>();
                            if (itemData2 != null && itemData2.size() > 0) {
                                for (int i = 0; i < itemData2.size(); i++) {
                                    String itemid = itemData2.get(i).get("itemid").asText();
                                    Double value = itemData2.get(i).get("value").asDouble();
                                    if (StringUtils.isBlank(itemid)) {
                                        continue;
                                    }
                                    if (avgValueMap.isEmpty() || avgValueMap.get(itemid) == null) {
                                        List<Double> avgs = new ArrayList<>();
                                        avgs.add(value);
                                        avgValueMap.put(itemid, avgs);
                                        continue;
                                    }
                                    if (!avgValueMap.isEmpty() && avgValueMap.get(itemid) != null) {
                                        List<Double> avgs = avgValueMap.get(itemid);
                                        avgs.add(value);
                                        avgValueMap.put(itemid, avgs);
                                    }
                                    for (MwTangibleassetsTable mwTangibleassetsDTO : mwTangibleassetsDTOS) {
                                        String assetsId = mwTangibleassetsDTO.getAssetsId();
                                        if (hostItemIds.contains(assetsId + itemid)) {
                                            RunTimeItemValue runTimeItemValue = new RunTimeItemValue();
//                                            List<HistoryValueDto> valueData = ReportUtil.getValueData(historyRsult);
//                                            TrendDto trendDtoNotUnit = ReportUtil.getTrendDtoNotUnit1(valueData);
//                                            String values = new BigDecimal(trendDtoNotUnit.getValueAvg()).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                                            runTimeItemValue.setItemName(name);
                                            runTimeItemValue.setItemId(itemid);
//                                            runTimeItemValue.setAvgValue(values);
//                                            runTimeItemValue.setSortLastAvgValue(Double.valueOf(trendDtoNotUnit.getValueAvg()));
                                            runTimeItemValue.setAssetName(mwTangibleassetsDTO.getAssetsName());
                                            runTimeItemValue.setServerId(mwTangibleassetsDTO.getMonitorServerId());
                                            runTimeItemValue.setIp(mwTangibleassetsDTO.getInBandIp());
                                            list.add(runTimeItemValue);
                                        }
                                    }
                                }
                            } else {
                                for (MwTangibleassetsTable tangibleassetsTable : mwTangibleassetsDTOS) {
                                    if (assetsIds.contains(tangibleassetsTable.getAssetsId())) {
                                        RunTimeItemValue runTimeItemValue = new RunTimeItemValue();
                                        runTimeItemValue.setItemName(name);
                                        runTimeItemValue.setAvgValue("0.00");
                                        runTimeItemValue.setSortLastAvgValue(Double.valueOf("0"));
                                        runTimeItemValue.setMaxValue("0.00");
                                        runTimeItemValue.setMinValue("0.00");
                                        runTimeItemValue.setAssetName(tangibleassetsTable.getAssetsName());
                                        runTimeItemValue.setServerId(tangibleassetsTable.getMonitorServerId());
                                        runTimeItemValue.setIp(tangibleassetsTable.getInBandIp());
                                        list.add(runTimeItemValue);
                                    }
                                }

                            }
                            if (!CollectionUtils.isEmpty(list)) {
                                for (RunTimeItemValue runTimeItemValue : list) {
                                    if ((StringUtils.isNotBlank(runTimeItemValue.getItemId()) && avgValueMap.get(runTimeItemValue.getItemId()) != null) || (StringUtils.isNotBlank(runTimeItemValue.getItemId()) && redisUtils.get(name + runTimeItemValue.getItemId()) == null)) {
                                        List<Double> agvgs = new ArrayList<>();
                                        if (avgValueMap.get(runTimeItemValue.getItemId()) != null) {
                                            agvgs.addAll(avgValueMap.get(runTimeItemValue.getItemId()));
                                        } else {
                                            agvgs.add(0.0);
                                        }
                                        double counum = 0;
                                        double counumSize = 0;
                                        double Max = 0;
                                        double min = 0;
                                        double getTime = 0;
                                        if (changeTime) {
                                            Map<String, Double> map = (Map<String, Double>) redisUtils.get(name + runTimeItemValue.getItemId());
                                            if (map != null) {
                                                counum = map.get("counum");
                                                counumSize = map.get("counumSize");
                                                Max = map.get("Max");
                                                min = map.get("min");
                                            }

                                        } else {
                                            min = 1000000000;
                                        }

                                        Map<String, Double> map = new HashMap<>();
                                        //总效率,总数,最大，最小
                                        counum = agvgs.stream().mapToDouble(Double::valueOf).sum() + counum;
                                        counumSize = agvgs.size() > 0 ? agvgs.size() : 0 + counumSize;
                                        Max = agvgs.stream().mapToDouble(Double::valueOf).max().getAsDouble() > Max ? agvgs.stream().mapToDouble(Double::valueOf).max().getAsDouble() : Max;
                                        min = agvgs.stream().mapToDouble(Double::valueOf).min().getAsDouble() > min ? min : agvgs.stream().mapToDouble(Double::valueOf).min().getAsDouble();
                                        getTime = new Date().getTime() / 1000;
                                        map.put("counum", counum);
                                        map.put("counumSize", counumSize);
                                        map.put("Max", Max);
                                        map.put("min", min);
                                        map.put("getTime", getTime);
                                        redisUtils.set(name + runTimeItemValue.getItemId(), map, 86400);
                                        String valueavg = String.valueOf(agvgs.stream().mapToDouble(Double::valueOf).average().getAsDouble());
                                        String values = new BigDecimal(valueavg).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                                        runTimeItemValue.setAvgValue(values);
                                        runTimeItemValue.setMaxValue(String.valueOf(Max));
                                        runTimeItemValue.setMinValue(String.valueOf(min));
                                        runTimeItemValue.setSortLastAvgValue(Double.valueOf(valueavg));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


        log.info("topN类型0查询zabbix结束" + new Date());
        if (!CollectionUtils.isEmpty(list)) {
            Iterator<RunTimeItemValue> iterator = list.iterator();
            while (iterator.hasNext()) {
                RunTimeItemValue next = iterator.next();
                if (next.getAvgValue() == null) {
                    iterator.remove();
                }
            }
        }
        log.info("topN类型为0,执行zabbix查询操作完成" + new Date());
        return list;
    }


    public List<RunTimeItemValue> getrunTimeMemory2(Integer userId, Long startTime, Long endTime) {
        log.info("topN类型为1,开始执行" + new Date());
        MwCommonAssetsDto mwCommonAssetsDto = new MwCommonAssetsDto();
        mwCommonAssetsDto.setUserId(userId);
        Map<String, Object> map = mwAssetsManager.getAssetsByUserId(mwCommonAssetsDto);
        Object assetsList = map.get("assetsList");
        List<RunTimeItemValue> list = new ArrayList<>();
        if (assetsList != null) {
            List<MwTangibleassetsTable> mwTangibleassetsDTOS = (List<MwTangibleassetsTable>) assetsList;
            if (mwTangibleassetsDTOS.size() == 0) {
                return null;
            }
            list = getThreadValue2(mwTangibleassetsDTOS, startTime, endTime);
        }
        log.info("topN类型为1,执行完成，返回数据" + new Date());
        return list;
    }


    private List<RunTimeItemValue> getThreadValue2(List<MwTangibleassetsTable> mwTangibleassetsDTOS, Long startTime, Long endTime) {
        log.info("topN类型为1,开始执行zabbix操作" + new Date());
        List<RunTimeItemValue> list = new ArrayList<>();
        //根据资产服务ID分组
        Map<Integer, List<String>> assetsMap = new HashMap<>();
        Map<String, MwTangibleassetsTable> dtoMap = new HashMap<>();
        for (MwTangibleassetsTable mwTangibleassetsDTO : mwTangibleassetsDTOS) {
            Integer monitorServerId = mwTangibleassetsDTO.getMonitorServerId();
            String assetsId = mwTangibleassetsDTO.getAssetsId();
            dtoMap.put(assetsId, mwTangibleassetsDTO);
            if (assetsMap.isEmpty() || assetsMap.get(monitorServerId) == null) {
                List<String> assetsIds = new ArrayList<>();
                assetsIds.add(assetsId);
                assetsMap.put(monitorServerId, assetsIds);
                continue;
            }
            if (!assetsMap.isEmpty() && assetsMap.get(monitorServerId) != null) {
                List<String> assetsIds = assetsMap.get(monitorServerId);
                assetsIds.add(assetsId);
                assetsMap.put(monitorServerId, assetsIds);
            }
        }
        if (assetsMap.isEmpty()) {
            return null;
        }
        log.info("topN类型为1,开始查询zabbix" + new Date());
        for (Map.Entry<Integer, List<String>> integerListEntry : assetsMap.entrySet()) {
            Integer monitorServerId = integerListEntry.getKey();
            List<String> hostIds = integerListEntry.getValue();
            List<String> names = new ArrayList<>();
            names.add("DISK_UTILIZATION");
            names.add("INTERFACE_IN_UTILIZATION");
            Map<Integer, List<String>> itemIdMap = new HashMap();
            for (String name : names) {
                log.info("topN类型为1,名称" + name + "开始查询zabbix" + new Date());
                long getZabbixStart = System.currentTimeMillis();
                MWZabbixAPIResult result0 = mwtpServerAPI.itemGetbyType(monitorServerId, name, hostIds, false);
                long getZabbixEnd = System.currentTimeMillis();
                log.info("topN类型为1,名称" + name + "开始查询zabbix结束" + new Date());
                log.info("topN类型为1,执行mwtpServerAPI.itemGetbyType方法,hostIds长度" + hostIds.size() + "查询名称" + name + "耗时" + (getZabbixEnd - getZabbixStart));
                if (result0.getCode() == 0) {
                    JsonNode itemData = (JsonNode) result0.getData();
                    if (itemData != null && itemData.size() > 0) {
                        Integer vlaueType = itemData.get(0).get("value_type").asInt();
                        for (int i = 0; i < itemData.size(); i++) {
                            RunTimeItemValue runTimeItemValue = new RunTimeItemValue();
                            String itemid = itemData.get(i).get("itemid").asText();
                            int typeIndex = itemData.get(i).get("name").asText().indexOf("]");
                            if (StringUtils.isBlank(itemid)) {
                                continue;
                            }
                            String typeName = itemData.get(i).get("name").asText().substring(1, typeIndex);
                            runTimeItemValue.setItemName(name);
                            runTimeItemValue.setItemId(itemid);
                            runTimeItemValue.setInterfaceName(typeName);
                            runTimeItemValue.setIp(dtoMap.get(itemData.get(i).get("hostid").asText()).getInBandIp());
                            runTimeItemValue.setAssetName(dtoMap.get(itemData.get(i).get("hostid").asText()).getAssetsName());
                            if ("DISK_UTILIZATION".equals(name)) {
                                runTimeItemValue.setDiskName(typeName);
                                runTimeItemValue.setIp(dtoMap.get(itemData.get(i).get("hostid").asText()).getInBandIp());
                            }
                            list.add(runTimeItemValue);
                            if (itemIdMap.isEmpty() || itemIdMap.get(vlaueType) == null) {
                                List<String> itemIds = new ArrayList<>();
                                itemIds.add(itemid);
                                itemIdMap.put(vlaueType, itemIds);
                                continue;
                            }
                            if (!itemIdMap.isEmpty() && itemIdMap.get(vlaueType) != null) {
                                List<String> itemIds = itemIdMap.get(vlaueType);
                                itemIds.add(itemid);
                                itemIdMap.put(vlaueType, itemIds);
                            }
                        }
                    }
                }
            }
            for (Map.Entry<Integer, List<String>> listEntry : itemIdMap.entrySet()) {
                Integer vlaueType = listEntry.getKey();
                List<String> itemIds = listEntry.getValue();
                log.info("topN类型为1,vlaueType名称" + vlaueType + "开始查询zabbix" + new Date());
                long getZabbixStart = System.currentTimeMillis();
                MWZabbixAPIResult historyRsult1 = mwtpServerAPI.HistoryGetByTimeAndTypeASC(monitorServerId, itemIds, startTime, endTime, vlaueType);
                long getZabbixEnd = System.currentTimeMillis();
                log.info("topN类型为1,vlaueType名称" + vlaueType + "开始查询zabbix结束" + new Date());
                log.info("topN类型为1,执行mwtpServerAPI.HistoryGetByTimeAndTypeASC方法,itemIds长度" + itemIds.size() + "查询vlaueType名称" + vlaueType + "耗时" + (getZabbixEnd - getZabbixStart));
                JsonNode itemData = (JsonNode) historyRsult1.getData();
                Map<String, List<Double>> avgValueMap = new HashMap<>();
                if (itemData != null && itemData.size() > 0) {
                    for (int i = 0; i < itemData.size(); i++) {
                        String itemid = itemData.get(i).get("itemid").asText();
                        Double value = itemData.get(i).get("value").asDouble();
                        if (StringUtils.isBlank(itemid)) {
                            continue;
                        }
                        if (avgValueMap.isEmpty() || avgValueMap.get(itemid) == null) {
                            List<Double> avgs = new ArrayList<>();
                            avgs.add(value);
                            avgValueMap.put(itemid, avgs);
                            continue;
                        }
                        if (!avgValueMap.isEmpty() && avgValueMap.get(itemid) != null) {
                            List<Double> avgs = avgValueMap.get(itemid);
                            avgs.add(value);
                            avgValueMap.put(itemid, avgs);
                        }
                    }
                } else {
                    for (RunTimeItemValue runTimeItemValue : list) {
                        if (itemIds.contains(runTimeItemValue.getItemId()) && !"DISK_UTILIZATION".equals(runTimeItemValue.getItemName())) {
                            runTimeItemValue.setSortLastAvgValue(Double.valueOf("0"));
                            runTimeItemValue.setAvgValue("0.00");
                            runTimeItemValue.setOutInterfaceAvgValue("0");
                        }
                        if (itemIds.contains(runTimeItemValue.getItemId()) && "DISK_UTILIZATION".equals(runTimeItemValue.getItemName())) {
                            runTimeItemValue.setSortLastAvgValue(Double.valueOf("0"));
                            runTimeItemValue.setAvgValue("0.00");
                            runTimeItemValue.setSortLastAvgValue(Double.valueOf("0"));
                        }
                    }
                }
                for (RunTimeItemValue runTimeItemValue : list) {
                    List<Double> agvgs = avgValueMap.get(runTimeItemValue.getItemId());
                    if (!CollectionUtils.isEmpty(agvgs) && !"DISK_UTILIZATION".equals(runTimeItemValue.getItemName())) {
                        String valueavg = String.valueOf(agvgs.stream().mapToDouble(Double::valueOf).average().getAsDouble());
                        String values1 = new BigDecimal(valueavg).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                        runTimeItemValue.setSortLastAvgValue(Double.valueOf(valueavg));
                        runTimeItemValue.setAvgValue(values1);
                        runTimeItemValue.setOutInterfaceAvgValue(values1);
                    }
                    if (!CollectionUtils.isEmpty(agvgs) && "DISK_UTILIZATION".equals(runTimeItemValue.getItemName())) {
                        String valueavg = String.valueOf(agvgs.stream().mapToDouble(Double::valueOf).average().getAsDouble());
                        String values = new BigDecimal(valueavg).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                        runTimeItemValue.setSortLastAvgValue(Double.valueOf(valueavg));
                        runTimeItemValue.setAvgValue(values);
                        runTimeItemValue.setSortLastAvgValue(Double.valueOf(valueavg));

                    }
                }
            }
        }
        log.info("topN类型为1,结束查询zabbix" + new Date());
        if (!CollectionUtils.isEmpty(list)) {
            Iterator<RunTimeItemValue> iterator = list.iterator();
            while (iterator.hasNext()) {
                RunTimeItemValue next = iterator.next();
                if (next.getAvgValue() == null) {
                    iterator.remove();
                }
            }
        }
        log.info("topN类型为1,执行zabbix操作完成" + new Date());
        return list;
    }


//    private List<RunTimeItemValue> getThreadValue2(MwTangibleassetsTable asset, Long startTime, Long endTime) {
//        List<RunTimeItemValue> list = new ArrayList<>();
////        RunTimeItemValue runTimeItemValue = new RunTimeItemValue();
//        Integer monitorServerId = asset.getMonitorServerId();
//        String hostId = asset.getAssetsId();
//        List<String> names = new ArrayList<>();
////        names.add("DISK_UTILIZATION");
////        names.add("INTERFACE_IN_UTILIZATION");
//        MWZabbixAPIResult result0 = mwtpServerAPI.itemGetbyNameList(monitorServerId, names, hostId, false);
////        MWZabbixAPIResult result0 = mwtpServerAPI.itemGetbyType(monitorServerId, name, hostId, false);
//        if (result0.getCode() == 0) {
//            JsonNode itemData = (JsonNode) result0.getData();
//            if (itemData.size() > 0) {
//                Integer vlaueType = itemData.get(0).get("value_type").asInt();
//                for (int i = 0; i < itemData.size(); i++) {
//                    RunTimeItemValue runTimeItemValue = new RunTimeItemValue();
//                    String itemid = itemData.get(i).get("itemid").asText();
//                    String name = itemData.get(i).get("name").asText();
//                    int typeIndex = itemData.get(i).get("name").asText().indexOf("]");
//                    String typeName = itemData.get(i).get("name").asText().substring(1, typeIndex);
//                    MWZabbixAPIResult historyRsult = mwtpServerAPI.HistoryGetByTimeAndType(monitorServerId, itemid, startTime, endTime, vlaueType);
//                    List<HistoryValueDto> valueData = ReportUtil.getValueData(historyRsult);
//                    TrendDto trendDtoNotUnit = ReportUtil.getTrendDtoNotUnit1(valueData);
//                    String values = new BigDecimal(trendDtoNotUnit.getValueAvg()).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
//                    runTimeItemValue.setItemName(name);
//                    runTimeItemValue.setAvgValue(values);
//                    runTimeItemValue.setSortLastAvgValue(Double.valueOf(trendDtoNotUnit.getValueAvg()));
////                    runTimeItemValue.setMaxValue(trendDtoNotUnit.getValueMax());
////                    runTimeItemValue.setMinValue(trendDtoNotUnit.getValueMin());
//                    runTimeItemValue.setAssetName(asset.getAssetsName());
//                    if ("DISK_UTILIZATION".equals(name)) {
//                        runTimeItemValue.setDiskName(typeName);
//                        runTimeItemValue.setIp(asset.getInBandIp());
//                    }
//                    if ("INTERFACE_IN_UTILIZATION".equals(name) || "INTERFACE_OUT_UTILIZATION".equals(name)) {
//                        MWZabbixAPIResult result1 = mwtpServerAPI.itemGetbyType(monitorServerId, "["+typeName+"]INTERFACE_OUT_UTILIZATION", hostId, false);
//                        if (result1.getCode() == 0) {
//                            JsonNode itemData1 = (JsonNode) result1.getData();
//                            if (itemData1.size() > 0) {
//                                Integer vlaueType1 = itemData1.get(0).get("value_type").asInt();
//
////                                RunTimeItemValue runTimeItemValue = new RunTimeItemValue();
//                                String itemid1 = itemData1.get(0).get("itemid").asText();
////                                int typeIndex1 = itemData.get(0).get("name").asText().indexOf("]");
////                                String typeName1 = itemData.get(0).get("name").asText().substring(1, typeIndex);
//                                MWZabbixAPIResult historyRsult1 = mwtpServerAPI.HistoryGetByTimeAndType(monitorServerId, itemid1, startTime, endTime, vlaueType1);
//                                List<HistoryValueDto> valueData1 = ReportUtil.getValueData(historyRsult1);
//                                TrendDto trendDtoNotUnit1 = ReportUtil.getTrendDtoNotUnit1(valueData1);
//                                String values1 = new BigDecimal(trendDtoNotUnit1.getValueAvg()).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
//                                runTimeItemValue.setOutInterfaceAvgValue(values1);
//                            }
//                        }
//                        runTimeItemValue.setInterfaceName(typeName);
//                        runTimeItemValue.setIp(asset.getInBandIp());
//                    }
//                    list.add(runTimeItemValue);
//                }
//
//            }
//        }
//        return list;
//    }


    public List<RunTimeItemValue> getrunTimeMemory3(Integer userId, Long startTime, Long endTime) {
        MwCommonAssetsDto mwCommonAssetsDto = new MwCommonAssetsDto();
        mwCommonAssetsDto.setUserId(userId);
        Map<String, Object> map = mwAssetsManager.getAssetsByUserId(mwCommonAssetsDto);
        Object assetsList = map.get("assetsList");
        List<RunTimeItemValue> list = new ArrayList<>();
        if (assetsList != null) {
            List<MwTangibleassetsTable> mwTangibleassetsDTOS = (List<MwTangibleassetsTable>) assetsList;
            if (mwTangibleassetsDTOS.size() == 0) {
                return null;
            }
            CopyOnWriteArrayList<Future<RunTimeItemValue>> futureList = new CopyOnWriteArrayList<>();
            ExecutorService executorService = Executors.newFixedThreadPool(20);
            for (MwTangibleassetsTable mwTangibleassetsDTO : mwTangibleassetsDTOS) {
                GetDataByThread<RunTimeItemValue> getDataByThread = new GetDataByThread<RunTimeItemValue>() {
                    @Override
                    public RunTimeItemValue call() throws Exception {
                        RunTimeItemValue threadValue = getThreadValue3(mwTangibleassetsDTO, startTime, endTime);
                        return threadValue;
                    }
                };
                Future<RunTimeItemValue> submit = executorService.submit(getDataByThread);
                futureList.add(submit);
            }

            for (Future<RunTimeItemValue> itemValueFuture : futureList) {
                try {
                    RunTimeItemValue itemValue = itemValueFuture.get(30, TimeUnit.SECONDS);
                    list.add(itemValue);
                } catch (Exception e) {
                    itemValueFuture.cancel(true);
                    executorService.shutdown();
                } finally {
                    executorService.shutdown();
                }
            }
        }
        return list;
    }


    private RunTimeItemValue getThreadValue3(MwTangibleassetsTable asset, Long startTime, Long endTime) {
        RunTimeItemValue runTimeItemValue = new RunTimeItemValue();
        QueryAssetsAvailableParam param = new QueryAssetsAvailableParam();
        param.setMonitorServerId(asset.getMonitorServerId());
        param.setInBandIp(asset.getInBandIp());
        param.setAssetsId(asset.getAssetsId());
        param.setId(asset.getId());
        QueryAssetsAvailableParam newParam = mwServerManager.getItemIdByAvailableItem(param);
        if (newParam.getItemId() != null && StringUtils.isNotEmpty(newParam.getItemId())) {

            Double sum = 0.0;
            List<MWItemHistoryDto> historyDtos = zabbixManger.HistoryGetByTimeAndHistory(newParam.getMonitorServerId(), newParam.getItemId(), startTime, endTime, newParam.getValue_type());
            if (historyDtos != null && historyDtos.size() > 0) {
                for (MWItemHistoryDto historyDto : historyDtos) {

                    if (historyDto.getLastValue() == 1L || historyDto.getLastValue() == 2L) {
                        sum++;
                    }
                }
                int size = historyDtos.size();

                String per = new BigDecimal(sum * 100 / size).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                runTimeItemValue.setAssetUtilization(per);
                runTimeItemValue.setAssetName(asset.getAssetsName());
                runTimeItemValue.setIp(asset.getInBandIp());
//                runTimeItemValue.setSortLastAvgValue((sum * 100 / size));
            }
        }

        return runTimeItemValue;
    }


    public List<RunTimeItemValue> getrunTimeMemory4(Integer userId, Long startTime, Long endTime) {
        MwCommonAssetsDto mwCommonAssetsDto = new MwCommonAssetsDto();
        mwCommonAssetsDto.setUserId(userId);
        Map<String, Object> map = mwAssetsManager.getAssetsByUserId(mwCommonAssetsDto);
        Object assetsList = map.get("assetsList");
        List<RunTimeItemValue> list = new ArrayList<>();
        if (assetsList != null) {
            List<MwTangibleassetsTable> mwTangibleassetsDTOS = (List<MwTangibleassetsTable>) assetsList;
            if (mwTangibleassetsDTOS.size() == 0) {
                return null;
            }

        }
        return list;
    }



    //获取分组数据
    public Integer getNum(String name) {
        Integer fenNum = 0;
        switch (name){
            case "CPU_UTILIZATION":
                fenNum = ((null == cpuNum) && (cpuNum > 0)) ? 0 : cpuNum;
                break;
            case "MEMORY_UTILIZATION":
                fenNum = ((null == memoryNum) && (memoryNum > 0)) ? 0 : memoryNum;
                break;
            case "ICMP_LOSS":
                fenNum = ((null == icmpNum) && (icmpNum > 0)) ? 0 : icmpNum;
                break;
            case "INTERFACE_OUT_UTILIZATION":
                fenNum = ((null == interfaceNum) && (interfaceNum > 0)) ? 0 : interfaceNum;
                break;
            case "INTERFACE_IN_UTILIZATION":
                fenNum = ((null == interfaceNum) && (interfaceNum > 0)) ? 0 : interfaceNum;
                break;
            case "DISK_UTILIZATION":
                fenNum = ((null == diskNum) && (diskNum > 0)) ? 0 : diskNum;
                break;
        }
        return fenNum;
    }
}
