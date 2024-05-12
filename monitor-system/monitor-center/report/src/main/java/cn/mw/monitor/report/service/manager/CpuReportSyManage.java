package cn.mw.monitor.report.service.manager;

import cn.mw.monitor.common.constant.ZabbixItemConstant;
import cn.mw.monitor.report.dao.MwReportDao;
import cn.mw.monitor.report.dto.CpuAndMemoryDto;
import cn.mw.monitor.report.dto.CpuAndMemoryDtos;
import cn.mw.monitor.report.dto.CpuAndMemorySyDto;
import cn.mw.monitor.report.dto.HistoryValueDto;
import cn.mw.monitor.report.dto.SolarTimeDto;
import cn.mw.monitor.report.dto.TrendDto;
import cn.mw.monitor.report.dto.TrendParam;
import cn.mw.monitor.report.param.ReportBase;
import cn.mw.monitor.report.service.GetDataByThread;
import cn.mw.monitor.report.service.detailimpl.ReportUtil;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.common.MWDateConstant;
import cn.mw.monitor.util.MWUtils;
import cn.mw.monitor.util.Units;
import cn.mw.monitor.common.util.GroupHosts;
import cn.mw.monitor.virtualization.dto.VmHostEnum;
import cn.mw.monitor.virtualization.service.impl.MwVirtualServiceImpl;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author xhy
 * @date 2020/12/30 17:18
 */
@Component
@Slf4j
public class CpuReportSyManage {
    private static final Logger logger = LoggerFactory.getLogger("cn/mw/monitor/report/CpuReportManager");

    @Resource
    private MwReportDao mwReportDao;

    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    @Autowired
    MwVirtualServiceImpl mwVirtualService;

    public List<CpuAndMemorySyDto> getCpuAndMemoryTrend(TrendParam trendParam) {
        List<CpuAndMemorySyDto> list = new ArrayList<>();
        List<MwTangibleassetsTable> mwTangibleassetsDTOS = trendParam.getMwTangibleassetsDTOS();
        if (mwTangibleassetsDTOS.size() == 0) {
            return list;
        }
        List<String> assetIds = new ArrayList<>();
        mwTangibleassetsDTOS.forEach(assets -> {
            assetIds.add(assets.getAssetsId());
        });
        //高级查询 使用多线程
        if (null != trendParam.getChooseTime() && trendParam.getChooseTime().size() > 0) {
            CopyOnWriteArrayList<Future<List<CpuAndMemorySyDto>>> futureList = new CopyOnWriteArrayList<>();
            ExecutorService executorService = Executors.newFixedThreadPool(mwTangibleassetsDTOS.size());
            mwTangibleassetsDTOS.forEach(assets -> {
                Integer monitorServerId = assets.getMonitorServerId();
                String hostid = assets.getAssetsId();
                List<String> chooseTime = trendParam.getChooseTime();
                Long startTime = MWUtils.getDate(chooseTime.get(0) + " " + MWDateConstant.BEGIN_TIME, MWDateConstant.NORM_DATETIME);
                Long endTime = MWUtils.getDate(chooseTime.get(1) + " " + MWDateConstant.END_TIME, MWDateConstant.NORM_DATETIME);
                if (assets.getAssetsTypeId() == 5) {//如果是虚拟化的资产，查询方式和普通的资产不一样
                    getVirtuaAssets(futureList, executorService, monitorServerId, hostid, startTime, endTime,trendParam.getParticle());
                } else {
                    GetDataByThread<List<CpuAndMemorySyDto>> getDataByThread = new GetDataByThread<List<CpuAndMemorySyDto>>() {
                        @Override
                        public List<CpuAndMemorySyDto> call() {
                            List<CpuAndMemorySyDto> dto = getCpuAndMemoryDto(assets, monitorServerId, hostid, startTime, endTime,trendParam.getParticle());
                            return dto;
                        }
                    };
                    Future<List<CpuAndMemorySyDto>> f = executorService.submit(getDataByThread);
                    futureList.add(f);
                }
            });
            List<CpuAndMemorySyDto> finalList = list;
            futureList.forEach(f -> {
                try {
                    List<CpuAndMemorySyDto> fd=f.get(10, TimeUnit.MINUTES);
                    fd.forEach(CpuAndMemorySyDto->finalList.add(CpuAndMemorySyDto));
//                    finalList.add(f.get(10, TimeUnit.MINUTES));
                } catch (Exception e) {
                    logger.error("getCpuAndMemoryTrend", e);
                    f.cancel(true);
                    executorService.shutdown();
                }
            });
            executorService.shutdown();
        }

        return list;
    }

    private List<CpuAndMemorySyDto> getCpuAndMemoryDto(MwTangibleassetsTable assets, Integer monitorServerId, String hostid, Long startTime, Long endTime,String particle) {
        List<CpuAndMemorySyDto> listdto = new ArrayList<>();
//        dto.setAssetsName(assets.getAssetsName());
//        dto.setIpAddress(assets.getInBandIp());
        //查询内存和CPU利用率的item
        MWZabbixAPIResult result0 = mwtpServerAPI.itemGetbyType(monitorServerId, "MEMORY_UTILIZATION", hostid, false);
        MWZabbixAPIResult result1 = mwtpServerAPI.itemGetbyType(monitorServerId, "CPU_UTILIZATION", hostid, false);

        long c= Integer.parseInt(particle)*60;
        Long timeBetween=endTime-startTime;
        int x=(int)Math.ceil(Float.parseFloat(timeBetween.toString())/c);

        List<HistoryValueDto> reslut = new ArrayList<>();
        JsonNode itemData = (JsonNode) result0.getData();
        if (result0.getCode() == 0&&itemData.size() > 0){
            long sumMemoryFree = 0;
            List<String> itemIds = new ArrayList<>();
            Integer vlaueType = itemData.get(0).get("value_type").asInt();
            for (int i = 0; i < itemData.size(); i++) {
                long memoryFree = 100 - itemData.get(i).get("lastvalue").asLong();
                sumMemoryFree += memoryFree;
                String itemid = itemData.get(i).get("itemid").asText();
                itemIds.add(itemid);
            }
//                    dto.setMemoryFreeRage(UnitsUtil.getValueWithUnits(String.valueOf(sumMemoryFree / itemData.size()), Units.PRECENT.getUnits()));
            //查询内存历史数据
            MWZabbixAPIResult historyRsult = mwtpServerAPI.HistoryGetByTimeAndType(monitorServerId, itemIds, startTime, endTime, vlaueType);
            reslut.addAll(ReportUtil.getValueTimeData(historyRsult));
        }

        JsonNode itemDataMerry = (JsonNode) result1.getData();
        List<HistoryValueDto> reslutMerry = new ArrayList<>();
        if (result1.getCode() == 0&&itemDataMerry.size() > 0) {
            List<String> itemIds = new ArrayList<>();
            long sumCpuFree = 0;
            Integer vlaueType = itemDataMerry.get(0).get("value_type").asInt();
            for (int i = 0; i < itemDataMerry.size(); i++) {
                long cpuFree = 100 - itemDataMerry.get(0).get("lastvalue").asLong();
                sumCpuFree += cpuFree;
                String itemid = itemDataMerry.get(0).get("itemid").asText();
                itemIds.add(itemid);
            }
//                    dto.setCpuFreeRage(UnitsUtil.getValueWithUnits(String.valueOf(sumCpuFree / itemData.size()), Units.PRECENT.getUnits()));
            //查询cpu的历史数据
            MWZabbixAPIResult historyRsult = mwtpServerAPI.HistoryGetByTimeAndType(monitorServerId, itemIds, startTime, endTime, vlaueType);
            reslutMerry.addAll(ReportUtil.getValueTimeData(historyRsult));
        }


        for(int j=0;j<x;j++) {
            CpuAndMemorySyDto dtos = new CpuAndMemorySyDto();
            dtos.setRecordTime(new Date(Long.valueOf(startTime+c*j)*1000L));
            dtos.setAssetsName(assets.getAssetsName());
            dtos.setIpAddress(assets.getInBandIp());
            int finalI = j;
            if (result0.getCode() == 0) {
                if (itemData.size() > 0) {
                    List<HistoryValueDto> list1 = reslut.stream().filter(s -> (startTime + c * finalI) <= s.getClock() && s.getClock() <= (startTime + c * (finalI + 1))).collect(Collectors.toList());
                    TrendDto trendDto = ReportUtil.getTrendDto(list1, Units.PRECENT.getUnits());
                    dtos.setMemoryAvgValue(trendDto.getValueAvg());
                    dtos.setMemoryMaxValue(trendDto.getValueMax());
                    dtos.setMemoryMinValue(trendDto.getValueMin());
                }
            }
            if (result1.getCode() == 0) {
                if (itemData.size() > 0) {
                    List<HistoryValueDto> list2 = reslutMerry.stream().filter(s -> (startTime + c * finalI) <= s.getClock() && s.getClock() <= (startTime + c * (finalI + 1))).collect(Collectors.toList());
                    TrendDto trendDto = ReportUtil.getTrendDto(list2, Units.PRECENT.getUnits());
                    dtos.setCpuAvgValue(trendDto.getValueAvg());
                    dtos.setCpuMaxValue(trendDto.getValueMax());
                    dtos.setCpuMinValue(trendDto.getValueMin());
                }
            }
            listdto.add(dtos);
        }

        return listdto;
    }


    private List<CpuAndMemorySyDto> getCpuAndMemoryDto1(MwTangibleassetsTable assets, Integer monitorServerId, String hostid, Long startTime, Long endTime,String particle) {
        List<CpuAndMemorySyDto> listdto = new ArrayList<>();
//        dto.setAssetsName(assets.getAssetsName());
//        dto.setIpAddress(assets.getInBandIp());
        //查询内存和CPU利用率的item
        MWZabbixAPIResult result0 = mwtpServerAPI.itemGetbyType(monitorServerId, "MEMORY_UTILIZATION", hostid, false);
        MWZabbixAPIResult result1 = mwtpServerAPI.itemGetbyType(monitorServerId, "CPU_UTILIZATION", hostid, false);

//        long c= Integer.parseInt(particle)*60*60;
//        Long timeBetween=endTime-startTime;
//        long x=timeBetween/c;
        long c= Integer.parseInt(particle)*60;
        Long timeBetween=endTime-startTime;
        int x=(int)Math.ceil(Float.parseFloat(timeBetween.toString())/c);
        for(int j=0;j<x;j++) {
            CpuAndMemorySyDto dtos = new CpuAndMemorySyDto();
            dtos.setRecordTime(new Date(Long.valueOf(startTime+c*j)*1000L));
            dtos.setAssetsName(assets.getAssetsName());
            dtos.setIpAddress(assets.getInBandIp());
            int finalI = j;

            if (result0.getCode() == 0) {
                JsonNode itemData = (JsonNode) result0.getData();
                if (itemData.size() > 0) {
                    long sumMemoryFree = 0;
                    List<String> itemIds = new ArrayList<>();
                    Integer vlaueType = itemData.get(0).get("value_type").asInt();
                    for (int i = 0; i < itemData.size(); i++) {
                        long memoryFree = 100 - itemData.get(i).get("lastvalue").asLong();
                        sumMemoryFree += memoryFree;
                        String itemid = itemData.get(i).get("itemid").asText();
                        itemIds.add(itemid);
                    }
//                    dto.setMemoryFreeRage(UnitsUtil.getValueWithUnits(String.valueOf(sumMemoryFree / itemData.size()), Units.PRECENT.getUnits()));
                    //查询内存历史数据
                    MWZabbixAPIResult historyRsult = mwtpServerAPI.HistoryGetByTimeAndType(monitorServerId, itemIds, startTime, endTime, vlaueType);
                    List<HistoryValueDto> valueData = ReportUtil.getValueData(historyRsult);
                    List<HistoryValueDto> list1 = valueData.stream().filter(s -> (startTime + c * finalI) <= s.getClock() && s.getClock() <= (startTime + c * (finalI + 1))).collect(Collectors.toList());
//                    List<HistoryValueDto> list2 = uvalueData.stream().filter(s -> (startTime + c * finalI) <= s.getClock() && s.getClock() <= (startTime + c * (finalI + 1))).collect(Collectors.toList());
//                    TrendDto mtrendDto = ReportUtil.getTrendDtoNotUnit(list1);
//                    TrendDto utrendDto = ReportUtil.getTrendDtoNotUnit(list2);
                    TrendDto trendDto = ReportUtil.getTrendDto(list1, Units.PRECENT.getUnits());
                    dtos.setMemoryAvgValue(trendDto.getValueAvg());
                    dtos.setMemoryMaxValue(trendDto.getValueMax());
                    dtos.setMemoryMinValue(trendDto.getValueMin());
                }
            }
            if (result1.getCode() == 0) {
                JsonNode itemData = (JsonNode) result1.getData();
                if (itemData.size() > 0) {
                    List<String> itemIds = new ArrayList<>();
                    long sumCpuFree = 0;
                    Integer vlaueType = itemData.get(0).get("value_type").asInt();
                    for (int i = 0; i < itemData.size(); i++) {
                        long cpuFree = 100 - itemData.get(0).get("lastvalue").asLong();
                        sumCpuFree += cpuFree;
                        String itemid = itemData.get(0).get("itemid").asText();
                        itemIds.add(itemid);
                    }
//                    dto.setCpuFreeRage(UnitsUtil.getValueWithUnits(String.valueOf(sumCpuFree / itemData.size()), Units.PRECENT.getUnits()));
                    //查询cpu的历史数据
                    MWZabbixAPIResult historyRsult = mwtpServerAPI.HistoryGetByTimeAndType(monitorServerId, itemIds, startTime, endTime, vlaueType);
                    List<HistoryValueDto> valueData = ReportUtil.getValueData(historyRsult);
                    List<HistoryValueDto> list2 = valueData.stream().filter(s -> (startTime + c * finalI) <= s.getClock() && s.getClock() <= (startTime + c * (finalI + 1))).collect(Collectors.toList());

                    TrendDto trendDto = ReportUtil.getTrendDto(list2, Units.PRECENT.getUnits());
                    dtos.setCpuAvgValue(trendDto.getValueAvg());
                    dtos.setCpuMaxValue(trendDto.getValueMax());
                    dtos.setCpuMinValue(trendDto.getValueMin());
                }
            }
            listdto.add(dtos);
        }

        return listdto;
    }
    /**
     * 非高级查询
     * 对从数据库中查询到的数据进行单位转换处理
     *
     * @param list
     * @param dtos
     * @return
     */
    private List<CpuAndMemoryDto> convertCpuAndMenory(List<CpuAndMemoryDto> list, List<CpuAndMemoryDtos> dtos) {
        dtos.forEach(dto -> {
            CpuAndMemoryDto cpuAndMemoryDto = CpuAndMemoryDto.builder()
                    .assetsName(dto.getAssetsName())
                    .ipAddress(dto.getIpAddress())
                    .cpuFreeRage(dto.getCpuFreeRage() + "%")
                    .memoryFreeRage(dto.getMemoryFreeRage() + "%")
                    .cpuAvgValue(dto.getCpuAvgValue() + "%")
                    .cpuMaxValue(dto.getCpuMaxValue() + "%")
                    .cpuMinValue(dto.getCpuMinValue() + "%")
                    .memoryMaxValue(dto.getMemoryMaxValue() + "%")
                    .memoryMinValue(dto.getMemoryMinValue() + "%")
                    .memoryAvgValue(dto.getCpuAvgValue() + "%")
                    .build();
            list.add(cpuAndMemoryDto);
        });
        return list;
    }

    /**
     * 查询虚拟化的资产
     *
     * @param futureList
     * @param executorService
     * @param monitorServerId
     * @param hostid
     * @param startTime
     * @param endTime
     * @return
     */
    private CopyOnWriteArrayList<Future<List<CpuAndMemorySyDto>>> getVirtuaAssets(CopyOnWriteArrayList<Future<List<CpuAndMemorySyDto>>> futureList, ExecutorService executorService, Integer monitorServerId, String hostid, Long startTime, Long endTime,String particle) {
        MWZabbixAPIResult dRuleByHostId = mwtpServerAPI.getDRuleByHostId(monitorServerId, hostid);
        JsonNode data = (JsonNode) dRuleByHostId.getData();
        List<GroupHosts> groupHostsList = new ArrayList<>();
        if (data.size() > 0) {
            for (JsonNode dRule : data) {
                if (dRule.get("name").asText().indexOf(ZabbixItemConstant.HOSTCOMPUTER) != -1) {
                    String name = "[分组]" + dRule.get("name").asText();
                    groupHostsList = mwVirtualService.getGroupHosts(monitorServerId, Arrays.asList(name));
                }
            }
        }

        for (GroupHosts hosts : groupHostsList) {
            GetDataByThread<List<CpuAndMemorySyDto>> getDataByThread = new GetDataByThread<List<CpuAndMemorySyDto>>() {
                @Override
                public List<CpuAndMemorySyDto> call() throws Exception {
                    List<CpuAndMemorySyDto> list1=null;
                    CpuAndMemorySyDto dto = new CpuAndMemorySyDto();
                    MWZabbixAPIResult groups = mwtpServerAPI.getHostByHostId(monitorServerId, hosts.getHostid(), hosts.getName());
                    JsonNode data = (JsonNode) groups.getData();
                    if (data.size() > 0) {
                        for (JsonNode host : data) {
                            dto.setHostId(host.get("hostid").asText());
                            String hostId = host.get("hostid").asText();
                            dto.setAssetsName(host.get("name").asText());
                            if (host.get("interfaces").size() > 0) {
                                dto.setIpAddress(host.get("interfaces").get(0).get("ip").asText());
                            }
                            //1,查询资产的itemid,2根据itemid查询history,3对history数据处理转换成可以展示的数据
                            list1= getCpuAndMemoryViryual(monitorServerId, hostId, dto, startTime, endTime,particle);

                        }
                    }
                    return list1;
                }
            };
            Future<List<CpuAndMemorySyDto>> f = executorService.submit(getDataByThread);
            futureList.add(f);
        }
        return futureList;
    }

    private List<CpuAndMemorySyDto> getCpuAndMemoryViryual(Integer monitorServerId, String hostId, CpuAndMemorySyDto dto, Long startTime, Long endTime,String particle) {
        //1,查询资产的itemid,
        List<CpuAndMemorySyDto> list=null;
        MWZabbixAPIResult zabbixAPIResult = mwtpServerAPI.itemGetbyType(monitorServerId, "MWVM_", hostId, false);
        if (zabbixAPIResult.getCode() == 0) {
            JsonNode node = (JsonNode) zabbixAPIResult.getData();
            Double cores = 0.0;
            Double freQuency = 0.0;
            Double usage = 0.0;
            Double mTotal = 0.0;
            Double mUsed = 0.0;
            String uItemId = "";
            String mItemId = "";
            Integer vlaueType = 0;
            String uunits = "";
            String munits = "";
            if (node.size() > 0) {
                for (JsonNode item : node) {
                    String name = item.get("name").asText();
                    if (name.equals(VmHostEnum.MWVM_CPU_CORES.getItemName())) {
                        cores = item.get("lastvalue").asDouble();
                    } else if (name.equals(VmHostEnum.MWVM_CPU_FREQUENCY.getItemName())) {
                        freQuency = item.get("lastvalue").asDouble();
                    } else if (name.equals(VmHostEnum.MWVM_CPU_USAGE.getItemName())) {
                        usage = item.get("lastvalue").asDouble();
                        uItemId = item.get("itemid").asText();
                        uunits = item.get("units").asText();
                        vlaueType = item.get("value_type").asInt();
                        log.info("====uunits==============+++++++++++++++++" + uunits);
                    } else if (name.equals(VmHostEnum.MWVM_MEMORY_TOTAL.getItemName())) {
                        mTotal = item.get("lastvalue").asDouble();
                    } else if (name.equals(VmHostEnum.MWVM_MEMORY_USED.getItemName())) {
                        mUsed = item.get("lastvalue").asDouble();
                        mItemId = item.get("itemid").asText();
                        munits = item.get("units").asText();
                        log.info("====munits==============+++++++++++++++++" + munits);
                    }
                }
            }

//            BigDecimal cbg = new BigDecimal((1 - (usage / (cores * freQuency))) * 100);
//            double cpu = cbg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
//            dto.setCpuFreeRage(cpu + "%");
//
//            BigDecimal mbg = new BigDecimal((1 - (mUsed / mTotal)) * 100);
//            double memory = mbg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
//            dto.setMemoryFreeRage(memory + "%");
            //2根据itemid查询history,
            MWZabbixAPIResult mResult = mwtpServerAPI.HistoryGetByTimeAndType(monitorServerId, mItemId, startTime, endTime, vlaueType);
            MWZabbixAPIResult uResult = mwtpServerAPI.HistoryGetByTimeAndType(monitorServerId, uItemId, startTime, endTime, vlaueType);

            //3对history数据处理转换成可以展示的数据
            List<HistoryValueDto> mvalueData = ReportUtil.getValueTimeData(mResult);
            List<HistoryValueDto> uvalueData = ReportUtil.getValueTimeData(uResult);

            long c= Integer.parseInt(particle)*60;
            Long timeBetween=endTime-startTime;
            int x=(int)Math.ceil(Float.parseFloat(timeBetween.toString())/c);
            for(int i=0;i<x;i++){
                CpuAndMemorySyDto dtos=dto;
                int finalI = i;
                List<HistoryValueDto> list1=mvalueData.stream().filter(s->(startTime+c* finalI)<=s.getClock()&&s.getClock()<=(startTime+c*(finalI +1))).collect(Collectors.toList());
                List<HistoryValueDto> list2=uvalueData.stream().filter(s->(startTime+c* finalI)<=s.getClock()&&s.getClock()<=(startTime+c*(finalI +1))).collect(Collectors.toList());
                TrendDto mtrendDto = ReportUtil.getTrendDtoNotUnit(list1);
                TrendDto utrendDto = ReportUtil.getTrendDtoNotUnit(list2);
                if (usage != 0 && (cores * freQuency) != 0) {
                    BigDecimal bigDecimal = new BigDecimal((Double.valueOf(utrendDto.getValueMin()) / (cores * freQuency)) * 100);
                    String min = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                    dtos.setCpuMinValue(min + "%");
                    BigDecimal bigDecimal1 = new BigDecimal((Double.valueOf(utrendDto.getValueAvg()) / (cores * freQuency)) * 100);
                    String avg = bigDecimal1.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                    dtos.setCpuAvgValue(avg + "%");
                    BigDecimal bigDecimal2 = new BigDecimal((Double.valueOf(utrendDto.getValueMax()) / (cores * freQuency)) * 100);
                    String max = bigDecimal2.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                    dtos.setCpuMaxValue(max + "%");

                }
                if (mUsed != 0 && mTotal != 0) {
                    BigDecimal bg = new BigDecimal((Double.valueOf(mtrendDto.getValueMin()) / mTotal) * 100);
                    String min = bg.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                    dtos.setMemoryMinValue(min + "%");
                    BigDecimal bg1 = new BigDecimal((Double.valueOf(mtrendDto.getValueAvg()) / mTotal) * 100);
                    String avg = bg1.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                    dtos.setMemoryAvgValue(avg + "%");
                    BigDecimal bg2 = new BigDecimal((Double.valueOf(mtrendDto.getValueMax()) / mTotal) * 100);
                    String max = bg2.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                    dtos.setMemoryMaxValue(max + "%");

                }
                dtos.setRecordTime(new Date(Long.valueOf(startTime+c*i)*1000L));
                list.add(dtos);
            }



        }
        return list;

    }

    public List<CpuAndMemoryDtos> getCpuAndMemoryTrends(List<MwTangibleassetsTable> mwTangibleassetsDTOS, Long startTime, Long endTime) {
        List<CpuAndMemoryDtos> list = new ArrayList<>();
        List<Future<CpuAndMemoryDtos>> futureList = new ArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(mwTangibleassetsDTOS.size());
        mwTangibleassetsDTOS.forEach(assets -> {
            String hostid = assets.getAssetsId();
            Integer monitorServerId = assets.getMonitorServerId();
            if (assets.getAssetsTypeId() == 5) {//如果是虚拟化的资产，查询方式和普通的资产不一样
                MWZabbixAPIResult dRuleByHostId = mwtpServerAPI.getDRuleByHostId(monitorServerId, hostid);
                JsonNode data = (JsonNode) dRuleByHostId.getData();
                List<GroupHosts> groupHostsList = new ArrayList<>();
                if (data.size() > 0) {
                    for (JsonNode dRule : data) {
                        if (dRule.get("name").asText().indexOf(ZabbixItemConstant.HOSTCOMPUTER) != -1) {
                            String name = "[分组]" + dRule.get("name").asText();
                            groupHostsList = mwVirtualService.getGroupHosts(monitorServerId, Arrays.asList(name));
                        }
                    }
                }
                for (GroupHosts hosts : groupHostsList) {
                    GetDataByThread<CpuAndMemoryDtos> getDataByThread = new GetDataByThread<CpuAndMemoryDtos>() {
                        @Override
                        public CpuAndMemoryDtos call() throws Exception {

                            return getCpuAndMemoryDtos(monitorServerId, hosts, startTime, endTime);
                        }
                    };
                    if (null != getDataByThread) {
                        Future<CpuAndMemoryDtos> f = executorService.submit(getDataByThread);
                        futureList.add(f);
                    }
                }
            } else {
                GetDataByThread<CpuAndMemoryDtos> getDataByThread = new GetDataByThread() {
                    @Override
                    public CpuAndMemoryDtos call() {
                        return getCpuAndMemoryDtos(assets, monitorServerId, hostid, startTime, endTime);
                    }
                };
                if (null != getDataByThread) {
                    Future<CpuAndMemoryDtos> f = executorService.submit(getDataByThread);
                    futureList.add(f);
                }
            }
        });
        futureList.forEach(f -> {
            try {
                CpuAndMemoryDtos cpuAndMemoryDtos = f.get(10, TimeUnit.MINUTES);
                if (null != cpuAndMemoryDtos) {
                    list.add(cpuAndMemoryDtos);

                }
            } catch (Exception e) {
                f.cancel(true);
                executorService.shutdown();
            }
        });
        executorService.shutdown();
        return list;
    }

    private CpuAndMemoryDtos getCpuAndMemoryDtos(MwTangibleassetsTable assets, Integer monitorServerId, String hostid, Long startTime, Long endTime) {
        CpuAndMemoryDtos dto = new CpuAndMemoryDtos();
        dto.setAssetsName(assets.getAssetsName());
        dto.setIpAddress(assets.getInBandIp());
        dto.setAssetsId(assets.getAssetsId());
        //查询item
        MWZabbixAPIResult result0 = mwtpServerAPI.itemGetbyType(monitorServerId, "MEMORY_UTILIZATION", hostid, false);
        MWZabbixAPIResult result1 = mwtpServerAPI.itemGetbyType(monitorServerId, "CPU_UTILIZATION", hostid, false);
        if (result0.getCode() == 0) {
            JsonNode itemData = (JsonNode) result0.getData();
            if (itemData.size() > 0) {
                List<String> itemIds = new ArrayList<>();
                Integer vlaueType = itemData.get(0).get("value_type").asInt();
                for (int i = 0; i < itemData.size(); i++) {
                    String itemid = itemData.get(i).get("itemid").asText();
                    itemIds.add(itemid);
                }
                //查询item历史
                MWZabbixAPIResult historyRsult = mwtpServerAPI.HistoryGetByTimeAndType(monitorServerId, itemIds, startTime, endTime, vlaueType);
                List<HistoryValueDto> valueData = ReportUtil.getValueData(historyRsult);
                double max = valueData.stream().mapToDouble(HistoryValueDto::getValue).average().getAsDouble();
                double min = valueData.stream().mapToDouble(HistoryValueDto::getValue).average().getAsDouble();
                double avg = valueData.stream().mapToDouble(HistoryValueDto::getValue).average().getAsDouble();

                dto.setMemoryAvgValue(new BigDecimal(avg).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                dto.setMemoryMaxValue(new BigDecimal(max).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                dto.setMemoryMinValue(new BigDecimal(min).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                dto.setMemoryFreeRage(new BigDecimal(100 - dto.getMemoryAvgValue()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
        }
        if (result1.getCode() == 0) {
            JsonNode itemData = (JsonNode) result1.getData();
            if (itemData.size() > 0) {
                List<String> itemIds = new ArrayList<>();
                Integer vlaueType = itemData.get(0).get("value_type").asInt();
                for (int i = 0; i < itemData.size(); i++) {
                    String itemid = itemData.get(0).get("itemid").asText();
                    itemIds.add(itemid);
                }
                //查询item历史
                MWZabbixAPIResult historyRsult = mwtpServerAPI.HistoryGetByTimeAndType(monitorServerId, itemIds, startTime, endTime, vlaueType);
                List<HistoryValueDto> valueData = ReportUtil.getValueData(historyRsult);
                if (valueData.size() > 0) {
                    double max = valueData.stream().mapToDouble(HistoryValueDto::getValue).max().getAsDouble();
                    double min = valueData.stream().mapToDouble(HistoryValueDto::getValue).min().getAsDouble();
                    double avg = valueData.stream().mapToDouble(HistoryValueDto::getValue).average().getAsDouble();
                    dto.setCpuAvgValue(new BigDecimal(avg).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    dto.setCpuMaxValue(new BigDecimal(max).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    dto.setCpuMinValue(new BigDecimal(min).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    dto.setCpuFreeRage(new BigDecimal(100 - dto.getCpuAvgValue()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                }
            }
        }
        return dto;
    }

    private CpuAndMemoryDtos getCpuAndMemoryDtos(Integer monitorServerId, GroupHosts hosts, Long startTime, Long endTime) {
        CpuAndMemoryDtos dto = new CpuAndMemoryDtos();
        MWZabbixAPIResult groups = mwtpServerAPI.getHostByHostId(monitorServerId, hosts.getHostid(), hosts.getName());
        JsonNode data = (JsonNode) groups.getData();
        if (data.size() > 0) {
            for (JsonNode host : data) {
                String hostId = host.get("hostid").asText();
                dto.setAssetsId(hostId);
                dto.setAssetsName(host.get("name").asText());
                if (host.get("interfaces").size() > 0) {
                    dto.setIpAddress(host.get("interfaces").get(0).get("ip").asText());
                }
                MWZabbixAPIResult zabbixAPIResult = mwtpServerAPI.itemGetbyType(monitorServerId, "MWVM_", hostId, false);
                if (zabbixAPIResult.getCode() == 0) {
                    JsonNode node = (JsonNode) zabbixAPIResult.getData();
                    Double cores = 0.0;
                    Double freQuency = 0.0;
                    Double usage = 0.0;
                    Double mTotal = 0.0;
                    Double mUsed = 0.0;
                    String uItemId = "";
                    String mItemId = "";
                    Integer vlaueType = 0;
                    String uunits = "";
                    String munits = "";
                    if (node.size() > 0) {
                        for (JsonNode item : node) {
                            String name = item.get("name").asText();
                            if (name.equals(VmHostEnum.MWVM_CPU_CORES.getItemName())) {
                                cores = item.get("lastvalue").asDouble();
                            } else if (name.equals(VmHostEnum.MWVM_CPU_FREQUENCY.getItemName())) {
                                freQuency = item.get("lastvalue").asDouble();
                            } else if (name.equals(VmHostEnum.MWVM_CPU_USAGE.getItemName())) {
                                usage = item.get("lastvalue").asDouble();
                                uItemId = item.get("itemid").asText();
                                uunits = item.get("units").asText();
                                vlaueType = item.get("value_type").asInt();
                                log.info("====uunits==============+++++++++++++++++" + uunits);
                            } else if (name.equals(VmHostEnum.MWVM_MEMORY_TOTAL.getItemName())) {
                                mTotal = item.get("lastvalue").asDouble();
                            } else if (name.equals(VmHostEnum.MWVM_MEMORY_USED.getItemName())) {
                                mUsed = item.get("lastvalue").asDouble();
                                mItemId = item.get("itemid").asText();
                                munits = item.get("units").asText();
                                log.info("====munits==============+++++++++++++++++" + munits);
                            }
                        }
                    }

                    MWZabbixAPIResult mResult = mwtpServerAPI.HistoryGetByTimeAndType(monitorServerId, mItemId, startTime, endTime, vlaueType);
                    MWZabbixAPIResult uResult = mwtpServerAPI.HistoryGetByTimeAndType(monitorServerId, uItemId, startTime, endTime, vlaueType);

                    List<HistoryValueDto> mvalueData = ReportUtil.getValueData(mResult);
                    List<HistoryValueDto> uvalueData = ReportUtil.getValueData(uResult);

                    TrendDto mtrendDto = ReportUtil.getTrendDtoNotUnit(mvalueData);
                    TrendDto utrendDto = ReportUtil.getTrendDtoNotUnit(uvalueData);

                    if (usage != 0 && (cores * freQuency) != 0) {
                        BigDecimal bigDecimal = new BigDecimal((Double.valueOf(utrendDto.getValueMin()) / (cores * freQuency)) * 100);
                        Double min = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                        dto.setCpuMinValue(min);
                        BigDecimal bigDecimal1 = new BigDecimal((Double.valueOf(utrendDto.getValueAvg()) / (cores * freQuency)) * 100);
                        Double avg = bigDecimal1.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                        dto.setCpuAvgValue(avg);
                        BigDecimal bigDecimal2 = new BigDecimal((Double.valueOf(utrendDto.getValueMax()) / (cores * freQuency)) * 100);
                        Double max = bigDecimal2.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                        dto.setCpuMaxValue(max);
                        dto.setCpuFreeRage(100 - avg);
                    }
                    if (mUsed != 0 && mTotal != 0) {
                        BigDecimal bg = new BigDecimal((Double.valueOf(mtrendDto.getValueMin()) / mTotal) * 100);
                        Double min = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                        dto.setMemoryMinValue(min);
                        BigDecimal bg1 = new BigDecimal((Double.valueOf(mtrendDto.getValueAvg()) / mTotal) * 100);
                        Double avg = bg1.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                        dto.setMemoryAvgValue(avg);
                        BigDecimal bg2 = new BigDecimal((Double.valueOf(mtrendDto.getValueMax()) / mTotal) * 100);
                        Double max = bg2.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                        dto.setMemoryMaxValue(max);

                        dto.setMemoryFreeRage(100 - avg);
                    }
                }
            }
        }
        return dto;
    }

    /**
     * 导入cpu和内存的zabbix历史数据到数据库
     *
     * @param mwTangibleassetsDTOS
     * @param startFrom
     * @param endTill
     * @param dateTime
     */
    public void inputCpuAndMemory(List<MwTangibleassetsTable> mwTangibleassetsDTOS, Long startFrom, Long endTill, Date dateTime) {
        List<CpuAndMemoryDtos> cpuAndMemoryTrends = getCpuAndMemoryTrends(mwTangibleassetsDTOS, startFrom, endTill);

        SolarTimeDto solarTimeDto = mwReportDao.selectTime(ReportBase.CPUANDMEMORY.getId());
        String workStartTime = MWUtils.getSolarData(solarTimeDto.getStartHourTime(), solarTimeDto.getStartMinuteTime(), 0, MWDateConstant.NORM_DATETIME, -1);
        String workEndTime = MWUtils.getSolarData(solarTimeDto.getEndHourTime(), solarTimeDto.getEndMinuteTime(), 0, MWDateConstant.NORM_DATETIME, -1);
        Long workStartFrom = MWUtils.getDate(workStartTime, MWDateConstant.NORM_DATETIME);
        Long workEndTill = MWUtils.getDate(workEndTime, MWDateConstant.NORM_DATETIME);
        List<CpuAndMemoryDtos> workCpuAndMemoryTrends = getCpuAndMemoryTrends(mwTangibleassetsDTOS, workStartFrom, workEndTill);

        String solarData = MWUtils.getSolarData(0, 0, 0, MWDateConstant.NORM_DATE, -1);//昨天的日期
        int count = mwReportDao.selectSolarDayCount(solarData);//判断昨天是不是休息日 0 是工作日  1是休息日

        if (count == 0 && workCpuAndMemoryTrends.size() > 0) {
            /**
             * 工作日
             * 0-24小时的数据
             * 8-17（自定义时间段）点的数据
             */
            String workDayTableName = "mw_report_cpu_memory_workday";

            String workDayWorkTimeTableName = "mw_report_cpu_memory_workday_worktime";
            mwReportDao.insertReportCpuAndMemory(workDayTableName, dateTime, cpuAndMemoryTrends);
            mwReportDao.insertReportCpuAndMemory(workDayWorkTimeTableName, dateTime, workCpuAndMemoryTrends);
        }
        if (cpuAndMemoryTrends.size() > 0) {
            String allDayTableName = "mw_report_cpu_memory_allday";
            String allDayWorkTimeTableName = "mw_report_cpu_memory_allday_worktime";
            mwReportDao.insertReportCpuAndMemory(allDayTableName, dateTime, cpuAndMemoryTrends);
            mwReportDao.insertReportCpuAndMemory(allDayWorkTimeTableName, dateTime, workCpuAndMemoryTrends);
        }
    }


}

