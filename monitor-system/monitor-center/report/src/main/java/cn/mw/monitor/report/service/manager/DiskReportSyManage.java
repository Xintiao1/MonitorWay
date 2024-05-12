package cn.mw.monitor.report.service.manager;

import cn.mw.monitor.api.common.ItemUtil;
import cn.mw.monitor.common.constant.ZabbixItemConstant;
import cn.mw.monitor.report.dao.MwReportDao;
import cn.mw.monitor.report.dto.DiskDto;
import cn.mw.monitor.report.dto.HistoryValueDto;
import cn.mw.monitor.report.dto.ItemData;
import cn.mw.monitor.report.dto.SolarTimeDto;
import cn.mw.monitor.report.dto.TrendDiskSyDto;
import cn.mw.monitor.report.dto.TrendDto;
import cn.mw.monitor.report.dto.TrendParam;
import cn.mw.monitor.report.param.ReportBase;
import cn.mw.monitor.report.service.GetDataByThread;
import cn.mw.monitor.report.service.detailimpl.ReportUtil;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.common.MWDateConstant;
import cn.mw.monitor.util.MWUtils;
import cn.mw.monitor.util.Units;
import cn.mw.monitor.util.UnitsUtil;
import cn.mw.monitor.common.util.GroupHosts;
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
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DiskReportSyManage {
    private static final Logger logger = LoggerFactory.getLogger("cn/mw/monitor/report/DiskReportSyManager");

    @Resource
    private MwReportDao mwReportDao;

    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    @Autowired
    MwVirtualServiceImpl mwVirtualService;

    public List<TrendDiskSyDto> getDiskTrend(TrendParam trendParam) {
        List<TrendDiskSyDto> list = new ArrayList<>();
        List<MwTangibleassetsTable> mwTangibleassetsDTOS = trendParam.getMwTangibleassetsDTOS();
        if (mwTangibleassetsDTOS.size() == 0) {
            return list;
        }
        List<String> assetIds = new ArrayList<>();
        mwTangibleassetsDTOS.forEach(assets -> {
            assetIds.add(assets.getAssetsId());
        });
        //高级查询
        if (null != trendParam.getChooseTime() && trendParam.getChooseTime().size() > 0) {
            List<String> chooseTime = trendParam.getChooseTime();
            Long startTime = MWUtils.getDate(chooseTime.get(0) + " " + MWDateConstant.BEGIN_TIME, MWDateConstant.NORM_DATETIME);
            Long endTime = MWUtils.getDate(chooseTime.get(1) + " " + MWDateConstant.END_TIME, MWDateConstant.NORM_DATETIME);
            ExecutorService executorService = Executors.newFixedThreadPool(trendParam.getMwTangibleassetsDTOS().size());
            List<Future<List<TrendDiskSyDto>>> futureList = new ArrayList<>();
            trendParam.getMwTangibleassetsDTOS().forEach(assets -> {
                String hostid = assets.getAssetsId();
                Integer monitorServerId = assets.getMonitorServerId();
                if (assets.getAssetsTypeId() == 5) {//如果是虚拟化的资产，查询方式和普通的资产不一样
                    GetDataByThread<List<TrendDiskSyDto>> getDataByThread = new GetDataByThread() {
                        @Override
                        public List<TrendDiskSyDto> call() throws Exception {
                            List<TrendDiskSyDto> dtos = new ArrayList<>();
                            MWZabbixAPIResult dRuleByHostId = mwtpServerAPI.getDRuleByHostId(monitorServerId, hostid);
                            JsonNode data = (JsonNode) dRuleByHostId.getData();
                            List<GroupHosts> groupHostsList = new ArrayList<>();
                            if (data.size() > 0) {
                                for (JsonNode dRule : data) {
                                    if (dRule.get("name").asText().indexOf(ZabbixItemConstant.VMWARE) != -1) {
                                        String name = "[分组]" + dRule.get("name").asText().toUpperCase();
                                        groupHostsList = mwVirtualService.getGroupHosts(monitorServerId, Arrays.asList(name));
                                    }
                                }
                            }
                            for (GroupHosts groupHosts : groupHostsList) {
                                MWZabbixAPIResult apiResult = mwtpServerAPI.itemGetbyType(monitorServerId, "MW_DISK_", groupHosts.getHostid(), false);
                                JsonNode items = (JsonNode) apiResult.getData();
                                if (items.size() > 0) {
                                    HashSet<String> set = new HashSet();
                                    for (JsonNode item : items) {
                                        String name = item.get("name").asText();
                                        if (!set.contains(name.substring(1, name.indexOf("]")))) {
                                            set.add(name.substring(1, name.indexOf("]")));
                                            log.info("======================" + name.substring(1, name.indexOf("]")));
                                        }
                                    }
                                    for (String type : set) {
                                        MWZabbixAPIResult result1 = mwtpServerAPI.getItemDataByAppName(monitorServerId, groupHosts.getHostid(), "DISK", "[" + type + "]MW_DISK");
                                        String itemid = "";
                                        Integer valueType = 0;
                                        String diskFreeitemid = "";
                                        Integer diskFreeValueType = 0;
                                        String units = "";
                                        String total=null;
                                        if (result1.getCode() == 0) {
                                            JsonNode resultData = (JsonNode) result1.getData();
                                            if (resultData.size() > 0) {
                                                for (JsonNode item : resultData) {
                                                    if (item.get("name").asText().equals("[" + type + "]" + ItemUtil.DISKITEMNAME.get(0))) {//磁盘利用率
                                                        itemid = item.get("itemid").asText();
                                                        valueType = item.get("value_type").asInt();
                                                    }
                                                    if (item.get("name").asText().equals("[" + type + "]" + ItemUtil.DISKITEMNAME.get(2))) {//磁盘剩余容量  磁盘使用容量/总容量=利用率
                                                        diskFreeitemid = item.get("itemid").asText();
                                                        diskFreeValueType = item.get("value_type").asInt();
                                                        units = item.get("units").asText();
                                                    }
                                                    //由于当磁盘使用率持续10分钟大于90%，zabbix的最新数据对于它的监控的值就会为0所以取单位时间内的最大值
                                                    if (item.get("name").asText().equals("[" + type + "]" + ItemUtil.DISKITEMNAME.get(3))) {
//
                                                        total=UnitsUtil.getValueWithUnits(item.get("lastvalue").asText(), item.get("units").asText());

                                                    }
                                                }
                                                MWZabbixAPIResult historyRsult = mwtpServerAPI.HistoryGetByTimeAndType(monitorServerId, itemid, startTime, endTime, valueType);
                                                MWZabbixAPIResult historyRsult1 = mwtpServerAPI.HistoryGetByTimeAndType(monitorServerId, diskFreeitemid, startTime, endTime, diskFreeValueType);
                                                List<HistoryValueDto> valueData = ReportUtil.getValueTimeData(historyRsult);
                                                List<HistoryValueDto> valueData1 = ReportUtil.getValueTimeData(historyRsult1);


                                                long c= Integer.parseInt(trendParam.getParticle())*60;
                                                Long timeBetween=endTime-startTime;
                                                int x=(int)Math.ceil(Float.parseFloat(timeBetween.toString())/c);
                                                for(int i=0;i<x;i++){
//
                                                    int finalI = i;
                                                    valueData1.stream().filter(s->(startTime+c* finalI)<=s.getClock()&&s.getClock()<=(startTime+c*(finalI +1))).mapToDouble(HistoryValueDto::getValue).average().getAsDouble();
                                                    TrendDiskSyDto diskDto = new TrendDiskSyDto();
                                                    diskDto.setIpAddress(assets.getInBandIp());
                                                    diskDto.setAssetsName(groupHosts.getName());
                                                    diskDto.setTypeName(type);
                                                    diskDto.setDiskTotal(total);
                                                    if (valueData1.size() > 0) {
                                                        diskDto.setDiskFree(UnitsUtil.getValueWithUnits(String.valueOf(valueData1.stream().filter(s->(startTime+c* finalI)<=s.getClock()&&s.getClock()<=(startTime+c*(finalI +1))).
                                                                mapToDouble(HistoryValueDto::getValue).average().getAsDouble()), units));
                                                    } else {
                                                        diskDto.setDiskFree("0.00B");
                                                    }
                                                    List<HistoryValueDto> lists=valueData.stream().filter(s->(startTime+c* finalI)<=s.getClock()&&s.getClock()<=(startTime+c*(finalI +1))).collect(Collectors.toList());
                                                    TrendDto trendDto = ReportUtil.getTrendDto(lists, Units.PRECENT.getUnits());
                                                    diskDto.setDiskAvgValue(trendDto.getValueAvg());
                                                    diskDto.setDiskMinValue(trendDto.getValueMin());
                                                    diskDto.setDiskMaxValue(trendDto.getValueMax());
                                                    diskDto.setRecordTime(new Date(Long.valueOf(startTime+c*i)*1000L));
                                                    dtos.add(diskDto);
                                                }

                                            }
                                        }
                                    }
                                }
                            }
                            return dtos;
                        }
                    };
                    if (null != getDataByThread) {
                        Future<List<TrendDiskSyDto>> f = executorService.submit(getDataByThread);
                        futureList.add(f);
                    }
                } else {
                    GetDataByThread<List<TrendDiskSyDto>> getDataByThread = new GetDataByThread() {
                        @Override
                        public List<TrendDiskSyDto> call() {
                            MWZabbixAPIResult result = mwtpServerAPI.itemgetbyhostid(monitorServerId, hostid, "DISK_INFO", true);
                            List<TrendDiskSyDto> dtos = new ArrayList<>();
                            if (result.getCode() == 0) {
                                List<ItemData> dataResult = ReportUtil.getItemDataResult(result);
                                if (dataResult.size() > 0) {
                                    for (ItemData data : dataResult) {
//
                                        String type = data.getName().substring(1, data.getName().indexOf("]"));
//                                        trendDiskDto.setTypeName(type);
                                        MWZabbixAPIResult result1 = mwtpServerAPI.getItemDataByAppName(monitorServerId, hostid, "DISK", "[" + type + "]MW_");
                                        String itemid = "";
                                        Integer valueType = 0;
                                        String diskFreeitemid = "";
                                        Integer diskFreeValueType = 0;
                                        String units = "";
                                        String total=null;
                                        if (result1.getCode() == 0) {
                                            JsonNode resultData = (JsonNode) result1.getData();
                                            if (resultData.size() > 0) {
                                                for (JsonNode item : resultData) {
                                                    if (item.get("name").asText().equals("[" + type + "]" + ItemUtil.DISKITEMNAME.get(0))) {//磁盘利用率
                                                        itemid = item.get("itemid").asText();
                                                        valueType = item.get("value_type").asInt();
                                                    }
                                                    if (item.get("name").asText().equals("[" + type + "]" + ItemUtil.DISKITEMNAME.get(2))) {//磁盘使用容量  磁盘使用容量/总容量=利用率
                                                        diskFreeitemid = item.get("itemid").asText();
                                                        diskFreeValueType = item.get("value_type").asInt();
                                                        units = item.get("units").asText();
                                                    }
                                                    if (item.get("name").asText().equals("[" + type + "]" + ItemUtil.DISKITEMNAME.get(3))) {
//
                                                        total=UnitsUtil.getValueWithUnits(item.get("lastvalue").asText(), item.get("units").asText());
                                                    }
                                                }

                                                MWZabbixAPIResult historyRsult = mwtpServerAPI.HistoryGetByTimeAndType(monitorServerId, itemid, startTime, endTime, valueType);
                                                MWZabbixAPIResult historyRsult1 = mwtpServerAPI.HistoryGetByTimeAndType(monitorServerId, diskFreeitemid, startTime, endTime, diskFreeValueType);
                                                List<HistoryValueDto> valueData = ReportUtil.getValueTimeData(historyRsult);
                                                List<HistoryValueDto> valueData1 = ReportUtil.getValueTimeData(historyRsult1);

                                                long c= Integer.parseInt(trendParam.getParticle())*60;
                                                Long timeBetween=endTime-startTime;
                                                int x=(int)Math.ceil(Float.parseFloat(timeBetween.toString())/c);
                                                for(int i=0;i<x;i++){
                                                    int finalI = i;
                                                    TrendDiskSyDto diskDto = new TrendDiskSyDto();
                                                    diskDto.setAssetsName(assets.getAssetsName());
                                                    diskDto.setIpAddress(assets.getInBandIp());
                                                    diskDto.setTypeName(type);
                                                    diskDto.setDiskTotal(total);
//
                                                    if (valueData1.size() > 0) {
                                                        diskDto.setDiskFree(UnitsUtil.getValueWithUnits(String.valueOf(valueData1.stream().filter(s->(startTime+c* finalI)<=s.getClock()&&s.getClock()<=(startTime+c*(finalI +1))).
                                                                mapToDouble(HistoryValueDto::getValue).average().getAsDouble()), units));
                                                    } else {
                                                        diskDto.setDiskFree("0.00B");
                                                    }
                                                    List<HistoryValueDto> lists=valueData.stream().filter(s->(startTime+c* finalI)<=s.getClock()&&s.getClock()<=(startTime+c*(finalI +1))).collect(Collectors.toList());

                                                    TrendDto trendDto = ReportUtil.getTrendDto(lists, Units.PRECENT.getUnits());
                                                    diskDto.setDiskAvgValue(trendDto.getValueAvg());
                                                    diskDto.setDiskMinValue(trendDto.getValueMin());
                                                    diskDto.setDiskMaxValue(trendDto.getValueMax());
                                                    diskDto.setRecordTime(new Date(Long.valueOf(startTime+c*i)*1000L));
                                                    dtos.add(diskDto);
                                                }

                                            }
                                        }

                                    }
                                    // return dtos;
                                }

                            }
                            return dtos;
                        }
                    };
                    if (null != getDataByThread) {
                        Future<List<TrendDiskSyDto>> f = executorService.submit(getDataByThread);
                        futureList.add(f);
                    }
                }
            });

            futureList.forEach(f -> {
                try {
                    List<TrendDiskSyDto> trendDiskDtos = f.get(60, TimeUnit.SECONDS);
                    trendDiskDtos.forEach(trendDiskDto -> {
                                list.add(trendDiskDto);
                            }
                    );
                } catch (Exception e) {
                    logger.error("getDiskTrend", e);
                    f.cancel(true);
                    executorService.shutdown();
                }
            });
            executorService.shutdown();
        }


        return list;

    }


    public List<DiskDto> getDiskTrends(List<MwTangibleassetsTable> mwTangibleassetsDTOS, Long startTime, Long endTime) {
        List<DiskDto> list = new ArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(mwTangibleassetsDTOS.size());
        List<Future<List<DiskDto>>> futureList = new ArrayList<>();
        mwTangibleassetsDTOS.forEach(assets -> {
            String hostid = assets.getAssetsId();
            Integer monitorServerId = assets.getMonitorServerId();
            if (assets.getAssetsTypeId() == 5) {//如果是虚拟化的资产，查询方式和普通的资产不一样
                GetDataByThread<List<DiskDto>> getDataByThread = new GetDataByThread() {
                    @Override
                    public List<DiskDto> call() throws Exception {
                        return getVDiskDtos(monitorServerId, hostid, assets, startTime, endTime);
                    }
                };
                if (null != getDataByThread) {
                    Future<List<DiskDto>> f = executorService.submit(getDataByThread);
                    futureList.add(f);
                }

            } else {
                GetDataByThread<List<DiskDto>> getDataByThread = new GetDataByThread() {
                    @Override
                    public List<DiskDto> call() {
                        return getDiskDto(monitorServerId, hostid, assets, startTime, endTime);
                    }
                };
                if (null != getDataByThread) {
                    Future<List<DiskDto>> f = executorService.submit(getDataByThread);
                    futureList.add(f);
                }
            }
        });

        futureList.forEach(f -> {
            try {
                List<DiskDto> trendDiskDtos = f.get(60, TimeUnit.SECONDS);
                if (null != trendDiskDtos) {
                    trendDiskDtos.forEach(trendDiskDto -> list.add(trendDiskDto));
                }
            } catch (Exception e) {
                f.cancel(true);
                executorService.shutdown();
            }
        });
        executorService.shutdown();
        return list;
    }

    private List<DiskDto> getDiskDto(Integer monitorServerId, String hostid, MwTangibleassetsTable assets, Long startTime, Long endTime) {
        List<DiskDto> dtos = new ArrayList<>();
        MWZabbixAPIResult result = mwtpServerAPI.itemgetbyhostid(monitorServerId, hostid, "DISK_INFO", true);
        if (result.getCode() == 0) {
            List<ItemData> dataResult = ReportUtil.getItemDataResult(result);
            if (dataResult.size() > 0) {
                for (ItemData data : dataResult) {
                    DiskDto diskDto = new DiskDto();
                    diskDto.setAssetsName(assets.getAssetsName());
                    diskDto.setIpAddress(assets.getInBandIp());
                    diskDto.setAssetsId(assets.getAssetsId());
                    String type = data.getName().substring(1, data.getName().indexOf("]"));
                    diskDto.setTypeName(type);
                    MWZabbixAPIResult result1 = mwtpServerAPI.getItemDataByAppName(monitorServerId, hostid, "DISK", "[" + type + "]MW_");
                    String itemid = "";
                    Integer valueType = 0;
                    String diskFreeitemid = "";
                    Integer diskFreeValueType = 0;
                    String units = "";
                    if (result1.getCode() == 0) {
                        JsonNode resultData = (JsonNode) result1.getData();
                        if (resultData.size() > 0) {
                            for (JsonNode item : resultData) {
                                if (item.get("name").asText().equals("[" + type + "]" + ItemUtil.DISKITEMNAME.get(0))) {//磁盘利用率
                                    itemid = item.get("itemid").asText();
                                    valueType = item.get("value_type").asInt();
                                }
                                if (item.get("name").asText().equals("[" + type + "]" + ItemUtil.DISKITEMNAME.get(2))) {//磁盘剩余容量
                                    diskFreeitemid = item.get("itemid").asText();
                                    diskFreeValueType = item.get("value_type").asInt();
                                    units = item.get("units").asText();
                                }
                                if (item.get("name").asText().equals("[" + type + "]" + ItemUtil.DISKITEMNAME.get(3))) {
                                    diskDto.setDiskTotal(item.get("lastvalue").asDouble());
//                                    String  diskTotalitemid = item.get("itemid").asText();
//                                    Integer diskTotalValueType = item.get("value_type").asInt();
//                                    if(item.get("lastvalue").toString().equals("0")|| StringUtils.isEmpty(item.get("lastvalue").toString())) {
//                                        MWZabbixAPIResult totalhistoryRsult = mwtpServerAPI.HistoryGetByTimeAndType(monitorServerId, diskTotalitemid, startTime, endTime, diskTotalValueType);
//                                        List<HistoryValueDto> totalValueData = ReportUtil.getValueData(totalhistoryRsult);
//                                        diskDto.setDiskTotal(totalValueData.stream().mapToDouble(HistoryValueDto::getValue).average().getAsDouble());
//                                    }else {
//                                        diskDto.setDiskTotal(item.get("lastvalue").asDouble());
//                                    }
                                }
                            }
                        }
                    }
                    MWZabbixAPIResult historyRsult = mwtpServerAPI.HistoryGetByTimeAndType(monitorServerId, itemid, startTime, endTime, valueType);
                    List<HistoryValueDto> valueData = ReportUtil.getValueData(historyRsult);
                    MWZabbixAPIResult historyRsult1 = mwtpServerAPI.HistoryGetByTimeAndType(monitorServerId, diskFreeitemid, startTime, endTime, diskFreeValueType);
                    List<HistoryValueDto> valueData1 = ReportUtil.getValueData(historyRsult1);
                    double max = valueData.stream().mapToDouble(HistoryValueDto::getValue).max().getAsDouble();
                    double min = valueData.stream().mapToDouble(HistoryValueDto::getValue).min().getAsDouble();
                    double avg = valueData.stream().mapToDouble(HistoryValueDto::getValue).average().getAsDouble();
                    double avgFree = valueData1.stream().mapToDouble(HistoryValueDto::getValue).average().getAsDouble();
                    diskDto.setDiskFree(new BigDecimal(avgFree).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    diskDto.setDiskAvgValue(new BigDecimal(avg).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    diskDto.setDiskMinValue(new BigDecimal(min).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    diskDto.setDiskMaxValue(new BigDecimal(max).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    dtos.add(diskDto);

                }
                //  return dtos;
            }
        }
        return dtos;
    }

    private List<DiskDto> getVDiskDtos(Integer monitorServerId, String hostid, MwTangibleassetsTable assets, Long startTime, Long endTime) {
        List<DiskDto> dtos = new ArrayList<>();
        MWZabbixAPIResult dRuleByHostId = mwtpServerAPI.getDRuleByHostId(monitorServerId, hostid);
        JsonNode data = (JsonNode) dRuleByHostId.getData();
        List<GroupHosts> groupHostsList = new ArrayList<>();
        if (data.size() > 0) {
            for (JsonNode dRule : data) {
                if (dRule.get("name").asText().indexOf(ZabbixItemConstant.VMWARE) != -1) {
                    String name = "[分组]" + dRule.get("name").asText().toUpperCase();
                    groupHostsList = mwVirtualService.getGroupHosts(monitorServerId, Arrays.asList(name));
                }
            }
        }
        for (GroupHosts groupHosts : groupHostsList) {
            MWZabbixAPIResult apiResult = mwtpServerAPI.itemGetbyType(monitorServerId, "MW_DISK_", groupHosts.getHostid(), false);
            JsonNode items = (JsonNode) apiResult.getData();
            if (items.size() > 0) {
                HashSet<String> set = new HashSet();
                for (JsonNode item : items) {
                    String name = item.get("name").asText();
                    if (!set.contains(name.substring(1, name.indexOf("]")))) {
                        set.add(name.substring(1, name.indexOf("]")));
                        log.info("======================" + name.substring(1, name.indexOf("]")));
                    }
                }

                for (String type : set) {
                    DiskDto diskDto = new DiskDto();
                    diskDto.setIpAddress(assets.getInBandIp());
                    diskDto.setAssetsName(groupHosts.getName());
                    diskDto.setAssetsId(assets.getAssetsId());
                    diskDto.setTypeName(type);
                    MWZabbixAPIResult result1 = mwtpServerAPI.getItemDataByAppName(monitorServerId, groupHosts.getHostid(), "DISK", "[" + type + "]MW_DISK");
                    String itemid = "";
                    Integer valueType = 0;
                    String diskFreeitemid = "";
                    Integer diskFreeValueType = 0;
                    String units = "";
                    if (result1.getCode() == 0) {
                        JsonNode resultData = (JsonNode) result1.getData();
                        if (resultData.size() > 0) {
                            for (JsonNode item : resultData) {
                                if (item.get("name").asText().equals("[" + type + "]" + ItemUtil.DISKITEMNAME.get(0))) {//磁盘利用率
                                    itemid = item.get("itemid").asText();
                                    valueType = item.get("value_type").asInt();
                                }
                                if (item.get("name").asText().equals("[" + type + "]" + ItemUtil.DISKITEMNAME.get(2))) {//磁盘使用容量  磁盘使用容量/总容量=利用率
                                    diskFreeitemid = item.get("itemid").asText();
                                    diskFreeValueType = item.get("value_type").asInt();
                                    units = item.get("units").asText();
                                }
                                if (item.get("name").asText().equals("[" + type + "]" + ItemUtil.DISKITEMNAME.get(3))) {
                                    diskDto.setDiskTotal(item.get("lastvalue").asDouble());

//                                    String  diskTotalitemid = item.get("itemid").asText();
//                                    Integer diskTotalValueType = item.get("value_type").asInt();
//                                    if(item.get("lastvalue").toString().equals("0")|| StringUtils.isEmpty(item.get("lastvalue").toString())) {
//                                        MWZabbixAPIResult totalhistoryRsult = mwtpServerAPI.HistoryGetByTimeAndType(monitorServerId, diskTotalitemid, startTime, endTime, diskTotalValueType);
//                                        List<HistoryValueDto> totalValueData = ReportUtil.getValueData(totalhistoryRsult);
//                                        diskDto.setDiskTotal(totalValueData.stream().mapToDouble(HistoryValueDto::getValue).average().getAsDouble());
//                                    }else {
//                                        diskDto.setDiskTotal(item.get("lastvalue").asDouble());
//                                    }

                                }
                            }
                            MWZabbixAPIResult historyRsult = mwtpServerAPI.HistoryGetByTimeAndType(monitorServerId, itemid, startTime, endTime, valueType);
                            List<HistoryValueDto> valueData = ReportUtil.getValueData(historyRsult);
                            MWZabbixAPIResult historyRsult1 = mwtpServerAPI.HistoryGetByTimeAndType(monitorServerId, diskFreeitemid, startTime, endTime, diskFreeValueType);
                            List<HistoryValueDto> valueData1 = ReportUtil.getValueData(historyRsult1);
                            double max = valueData.stream().mapToDouble(HistoryValueDto::getValue).max().getAsDouble();
                            double min = valueData.stream().mapToDouble(HistoryValueDto::getValue).min().getAsDouble();
                            double avg = valueData.stream().mapToDouble(HistoryValueDto::getValue).average().getAsDouble();
                            double avgFree = valueData1.stream().mapToDouble(HistoryValueDto::getValue).average().getAsDouble();
                            diskDto.setDiskFree(new BigDecimal(avgFree).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                            diskDto.setDiskAvgValue(new BigDecimal(avg).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                            diskDto.setDiskMinValue(new BigDecimal(min).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                            diskDto.setDiskMaxValue(new BigDecimal(max).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                            dtos.add(diskDto);
                        }
                    }
                }
            }
        }
        return dtos;
    }

    /**
     * 导入zabbi的磁盘数据到数据库
     *
     * @param year
     * @param mouth
     * @param day
     * @param mwTangibleassetsDTOS
     * @param startFrom
     * @param endTill
     * @param dateTime
     */
    public void inputDisk(Integer year, Integer mouth, Integer day, List<MwTangibleassetsTable> mwTangibleassetsDTOS, Long startFrom, Long endTill, Date dateTime) {
        List<DiskDto> diskTrends = getDiskTrends(mwTangibleassetsDTOS, startFrom, endTill);
        SolarTimeDto solarTimeDto = mwReportDao.selectTime(ReportBase.DISK.getId());

        String workStartTime = MWUtils.getSolarData(solarTimeDto.getStartHourTime(), solarTimeDto.getStartMinuteTime(), 0, MWDateConstant.NORM_DATETIME, -1);
        String workEndTime = MWUtils.getSolarData(solarTimeDto.getEndHourTime(), solarTimeDto.getEndMinuteTime(), 0, MWDateConstant.NORM_DATETIME, -1);
        Long workStartFrom = MWUtils.getDate(workStartTime, MWDateConstant.NORM_DATETIME);
        Long workEndTill = MWUtils.getDate(workEndTime, MWDateConstant.NORM_DATETIME);

        List<DiskDto> workDiskTrends = getDiskTrends(mwTangibleassetsDTOS, workStartFrom, workEndTill);

        String solarData = MWUtils.getSolarData(0, 0, 0, MWDateConstant.NORM_DATE, year, mouth - 1, day);//存入选中日期的参数
        int count = mwReportDao.selectSolarDayCount(solarData);//判断昨天是不是休息日 0 是工作日  1是休息日

        if (count == 0) {
            /**
             * 工作日
             * 0-24小时的数据
             * 8-17（自定义时间段）点的数据
             */
            String workDayTableName = "mw_report_disk_workday";
            String workDayWorkTimeTableName = "mw_report_disk_workday_worktime";
            mwReportDao.insertReportDisk(workDayTableName, dateTime, diskTrends);
            mwReportDao.insertReportDisk(workDayWorkTimeTableName, dateTime, workDiskTrends);
        }
        String allDayTableName = "mw_report_disk_allday";
        String allDayWorkTimeTableName = "mw_report_disk_allday_worktime";
        mwReportDao.insertReportDisk(allDayTableName, dateTime, diskTrends);
        mwReportDao.insertReportDisk(allDayWorkTimeTableName, dateTime, workDiskTrends);
    }
}
