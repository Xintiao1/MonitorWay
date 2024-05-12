package cn.mw.monitor.report.service.manager;

import cn.mw.monitor.api.common.ItemUtil;
import cn.mw.monitor.report.dao.MwReportDao;
import cn.mw.monitor.report.dto.*;
import cn.mw.monitor.report.dto.linkdto.QueryDto;
import cn.mw.monitor.report.param.ReportBase;
import cn.mw.monitor.report.service.GetDataByThread;
import cn.mw.monitor.report.service.GetNetItemIdThread;
import cn.mw.monitor.report.service.detailimpl.ReportUtil;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.common.MWDateConstant;
import cn.mw.monitor.util.MWUtils;
import cn.mw.monitor.util.Units;
import cn.mw.monitor.util.UnitsUtil;
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
import java.util.*;
import java.util.concurrent.*;

/**
 * @author xhy
 * @date 2020/12/30 17:18
 */
@Component
@Slf4j
public class NetWorkReportManager {
    private static final Logger logger = LoggerFactory.getLogger("cn/mw/monitor/report/NetWorkReportManager");

    @Resource
    private MwReportDao mwReportDao;

    @Autowired
    private MWTPServerAPI mwtpServerAPI;


    public List<TrendNetDto> getNetTrend(TrendParam trendParam) {
        List<TrendNetDto> list = new ArrayList<>();
        List<MwTangibleassetsTable> mwTangibleassetsDTOS = trendParam.getMwTangibleassetsDTOS();
        if(mwTangibleassetsDTOS.size()==0){
            return list;
        }
        List<String> assetIds = new ArrayList<>();
        mwTangibleassetsDTOS.forEach(assets -> {
            assetIds.add(assets.getAssetsId());
        });
//        if (!trendParam.getSeniorchecked()) {//非高级查询
        if(null==trendParam.getParticle()){
            getNetWorkList(trendParam, list, assetIds);
        } else {//高级查询
            List<Future<List<TrendNetDto>>> futureList = new ArrayList<>();
            int threadSize = mwTangibleassetsDTOS.size() > 1 ? (mwTangibleassetsDTOS.size() / 2) : mwTangibleassetsDTOS.size();
            ExecutorService executorService = Executors.newFixedThreadPool(threadSize);
            mwTangibleassetsDTOS.forEach(assets -> {
                        String hostId = assets.getAssetsId();
                        Integer monitorServerId = assets.getMonitorServerId();
                        GetDataByThread<List<TrendNetDto>> getDataByThread = new GetDataByThread() {
                            @Override
                            public List<TrendNetDto> call() throws Exception {
                                MWZabbixAPIResult result = mwtpServerAPI.itemgetbyhostid(monitorServerId, hostId, "INTERFACES_INFO", true);
                                List<TrendNetDto> dtoList = new ArrayList<>();
                                if (result.getCode() == 0) {
                                    List<ItemData> dataResult = ReportUtil.getItemDataResult(result);
                                    if (dataResult.size() > 0) {
                                        List<Future<TrendNetDto>> futureList1 = new ArrayList<>();
                                        int threadSize = dataResult.size() > 1 ? (dataResult.size() / 2) : dataResult.size();
                                        ExecutorService executorService1 = Executors.newFixedThreadPool(threadSize);
                                        for (ItemData data : dataResult) {
                                            GetNetItemIdThread getNetItemIdThread = new GetNetItemIdThread() {
                                                @Override
                                                public TrendNetDto call() throws Exception {
                                                    return getTrendNetDto(assets, data, monitorServerId, hostId, trendParam);
                                                }
                                            };
                                            if (null != getNetItemIdThread) {
                                                Future<TrendNetDto> f = executorService1.submit(getNetItemIdThread);
                                                futureList1.add(f);
                                            }
                                        }
                                        futureList1.forEach(f -> {
                                            try {
                                                TrendNetDto trendNetDto1 = f.get(15, TimeUnit.SECONDS);
                                                if (null != trendNetDto1) {
                                                    dtoList.add(trendNetDto1);
                                                }
                                            } catch (Exception e) {
                                                f.cancel(true);
                                                executorService1.shutdown();
                                            }
                                        });
                                        executorService1.shutdown();
                                    }
                                }
                                if (dtoList.size() > 0) {
                                    return dtoList;
                                }
                                return null;
                            }
                        };
                        if (null != getDataByThread) {
                            Future<List<TrendNetDto>> f = executorService.submit(getDataByThread);
                            futureList.add(f);
                        }
                    }
            );
            futureList.forEach(f -> {
                try {
                    List<TrendNetDto> trendNetDtoList = f.get(60, TimeUnit.SECONDS);
                    if (null != trendNetDtoList && trendNetDtoList.size() > 0) {
                        trendNetDtoList.forEach(trendNetDto -> list.add(trendNetDto));
                    }
                } catch (Exception e) {
                    f.cancel(true);
                }
            });
            executorService.shutdown();
        }
        return list;

    }


    private TrendNetDto getTrendNetDto(MwTangibleassetsTable assets, ItemData data, Integer monitorServerId, String hostId, TrendParam trendParam) {
        TrendNetDto dto = new TrendNetDto();
        dto.setAssetsName(assets.getAssetsName());
        dto.setIpAddress(assets.getInBandIp());
        dto.setAssetsId(assets.getAssetsId());
        dto.setAssetsTypeId(assets.getAssetsTypeId());
        String type = data.getName().substring(1, data.getName().indexOf("]"));
        dto.setNetName(type);
        MWZabbixAPIResult result1 = mwtpServerAPI.getItemDataByAppName(monitorServerId, hostId, "INTERFACES", "[" + type + "]MW_");
        Map<String, Object> map1 = new HashMap<>();
        if (result1.getCode() == 0) {
            JsonNode resultData = (JsonNode) result1.getData();
            if (resultData.size() > 0) {
                resultData.forEach(item -> {
                    if (item.get("name").asText().equals("[" + type + "]" + ItemUtil.METITEMNAME.get(1))) {
                        map1.put("in", item.get("itemid").asText());
                        map1.put("invalueType", item.get("value_type").asInt());
                        map1.put("units", item.get("units").asText());
                    }
                    if (item.get("name").asText().equals("[" + type + "]" + ItemUtil.METITEMNAME.get(2))) {
                        map1.put("out", item.get("itemid").asText());
                        map1.put("outvalueType", item.get("value_type").asInt());

                    }
                });
                List<String> chooseTime = trendParam.getChooseTime();
                Long startTime = MWUtils.getDate(chooseTime.get(0) + " " + MWDateConstant.BEGIN_TIME, MWDateConstant.NORM_DATETIME);
                Long endTime = MWUtils.getDate(chooseTime.get(1) + " " + MWDateConstant.END_TIME, MWDateConstant.NORM_DATETIME);
                MWZabbixAPIResult in = mwtpServerAPI.HistoryGetByTimeAndType(monitorServerId, map1.get("in").toString(), startTime, endTime, (Integer) map1.get("invalueType"));
                MWZabbixAPIResult out = mwtpServerAPI.HistoryGetByTimeAndType(monitorServerId, map1.get("out").toString(), startTime, endTime, (Integer) map1.get("outvalueType"));
                List<HistoryValueDto> inData = ReportUtil.getValueData(in);
                List<HistoryValueDto> outData = ReportUtil.getValueData(out);
                TrendDto inTrendDto = ReportUtil.getTrendDto(inData, map1.get("units").toString());
                TrendDto outTrendDto = ReportUtil.getTrendDto(outData, map1.get("units").toString());

                dto.setNetInBpsAvgValue(inTrendDto.getValueAvg());
                dto.setNetInBpsMaxValue(inTrendDto.getValueMax());
                dto.setNetInBpsMinValue(inTrendDto.getValueMin());
                dto.setNetOutBpsAvgValue(outTrendDto.getValueAvg());
                dto.setNetOutBpsMaxValue(outTrendDto.getValueMax());
                dto.setNetOutBpsMinValue(outTrendDto.getValueMin());
            }
            return dto;
        }
        return null;
    }

    private void getNetWorkList(TrendParam trendParam, List<TrendNetDto> list, List<String> assetIds) {
        String tableName = "";
        if (trendParam.getDayType() == 0) {//全天(小时)
            tableName = "mw_report_network_allday";
        } else if (trendParam.getDayType() == 1) { //全天(指定时间的段）
            tableName = "mw_report_network_allday_worktime";
        } else if (trendParam.getDayType() == 2) {// 工作日(24小时)
            tableName = "mw_report_network_workday";
        } else if (trendParam.getDayType() == 3) {// 工作日(指定时间的段）
            tableName = "mw_report_network_workday_worktime";
        }

        QueryDto queryDto =null;
        if(null!=trendParam.getChooseTime()&&trendParam.getChooseTime().size()>0) {
            queryDto = QueryDto.builder()
                    .tableName(tableName)
                    .objectIds(assetIds)
                    .startTime(trendParam.getChooseTime().get(0))
                    .endTime(trendParam.getChooseTime().get(1)).
                            build();
        }else{
            queryDto = QueryDto.builder()
                    .tableName(tableName)
                    .objectIds(assetIds).
                            build();
        }
        List<NetWorkDto> netWorkDtos = mwReportDao.selectNetWorkList(queryDto);
        if (null != netWorkDtos && netWorkDtos.size() > 0) {
            netWorkDtos.forEach(netDto -> {
                TrendNetDto trendNetDto = TrendNetDto.builder()
                        .assetsName(netDto.getAssetsName())
                        .ipAddress(netDto.getIpAddress())
                        .netName(netDto.getNetName())
                        .netInBpsMaxValue(UnitsUtil.getValueWithUnits(String.valueOf(netDto.getNetInBpsMaxValue()).toString(), Units.bps.getUnits()))
                        .netInBpsMinValue(UnitsUtil.getValueWithUnits(String.valueOf(netDto.getNetInBpsMinValue()), Units.bps.getUnits()))
                        .netInBpsAvgValue(UnitsUtil.getValueWithUnits(String.valueOf(netDto.getNetInBpsAvgValue()), Units.bps.getUnits()))
                        .netOutBpsMaxValue(UnitsUtil.getValueWithUnits(String.valueOf(netDto.getNetOutBpsMaxValue()), Units.bps.getUnits()))
                        .netOutBpsMinValue(UnitsUtil.getValueWithUnits(String.valueOf(netDto.getNetOutBpsMinValue()), Units.bps.getUnits()))
                        .netOutBpsAvgValue(UnitsUtil.getValueWithUnits(String.valueOf(netDto.getNetOutBpsAvgValue()), Units.bps.getUnits()))
                        .build();
                list.add(trendNetDto);
            });
        }

    }

    public List<NetWorkDto> getNetTrends(List<MwTangibleassetsTable> mwTangibleassetsDTOS, Long startTime, Long endTime) {
        logger.info("开始执行接口查询");
        List<Future<List<NetWorkDto>>> futureList = new ArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(mwTangibleassetsDTOS.size());
        List<NetWorkDto> list = new ArrayList<>();
        logger.info("开始根据hostid查询对应的itemid");
        mwTangibleassetsDTOS.forEach(assets -> {
                    String hostId = assets.getAssetsId();
                    Integer monitorServerId = assets.getMonitorServerId();
                    GetDataByThread<List<NetWorkDto>> getDataByThread = new GetDataByThread() {
                        @Override
                        public List<NetWorkDto> call() throws Exception {
                            MWZabbixAPIResult result = mwtpServerAPI.itemgetbyhostid(monitorServerId, hostId, "INTERFACES_INFO", true);
                            List<NetWorkDto> dtoList = new ArrayList<>();
                            if (result.getCode() == 0) {
                                List<ItemData> dataResult = ReportUtil.getItemDataResult(result);
                                if (dataResult.size() > 0) {
                                    List<Future<NetWorkDto>> futureList1 = new ArrayList<>();
                                    ExecutorService executorService1 = Executors.newFixedThreadPool(dataResult.size());
                                    for (ItemData data : dataResult) {
                                        GetNetItemIdThread getNetItemIdThread = new GetNetItemIdThread<NetWorkDto>() {
                                            @Override
                                            public NetWorkDto call() throws Exception {
                                                return getNetWorkDto(assets, data, monitorServerId, hostId, startTime, endTime);
                                            }
                                        };
                                        if (null != getNetItemIdThread) {
                                            Future<NetWorkDto> f = executorService1.submit(getNetItemIdThread);
                                            futureList1.add(f);
                                        }
                                    }

                                    futureList1.forEach(f -> {
                                        try {
                                            NetWorkDto netWorkDto = f.get(20, TimeUnit.MINUTES);
                                            if (null != netWorkDto) {
                                                dtoList.add(netWorkDto);
                                            }
                                        } catch (Exception e) {
                                            f.cancel(true);
                                            executorService1.shutdown();
                                        }
                                    });
                                    executorService1.shutdown();
                                }
                            }
                            return dtoList;
                        }
                    };
                    Future<List<NetWorkDto>> f = executorService.submit(getDataByThread);
                    futureList.add(f);
                }
        );
        futureList.forEach(f -> {
            try {
                List<NetWorkDto> netDtoList = f.get(15, TimeUnit.MINUTES);
                if (null != netDtoList && netDtoList.size() > 0) {
                    netDtoList.forEach(netWorkDto -> list.add(netWorkDto));
                }
            } catch (Exception e) {
                f.cancel(true);
                executorService.shutdown();

            }
        });
        executorService.shutdown();
        return list;
    }

    private NetWorkDto getNetWorkDto(MwTangibleassetsTable assets, ItemData data, Integer monitorServerId, String hostId, Long startTime, Long endTime) {
        NetWorkDto dto = new NetWorkDto();
        dto.setAssetsName(assets.getAssetsName());
        dto.setIpAddress(assets.getInBandIp());
        dto.setAssetsId(assets.getAssetsId());
        String type = data.getName().substring(1, data.getName().indexOf("]"));
        dto.setNetName(type);
        MWZabbixAPIResult result1 = mwtpServerAPI.getItemDataByAppName(monitorServerId, hostId, "INTERFACES", "[" + type + "]MW_");
        Map<String, Object> map1 = new HashMap<>();
        if (result1.getCode() == 0) {
            JsonNode resultData = (JsonNode) result1.getData();
            if (resultData.size() > 0) {
                resultData.forEach(item -> {
                    if (item.get("name").asText().equals("[" + type + "]" + ItemUtil.METITEMNAME.get(1))) {
                        map1.put("in", item.get("itemid").asText());
                        map1.put("invalueType", item.get("value_type").asInt());
                        map1.put("units", item.get("units").asText());
                    }
                    if (item.get("name").asText().equals("[" + type + "]" + ItemUtil.METITEMNAME.get(2))) {
                        map1.put("out", item.get("itemid").asText());
                        map1.put("outvalueType", item.get("value_type").asInt());
                    }
                });

                MWZabbixAPIResult in = mwtpServerAPI.HistoryGetByTimeAndType(monitorServerId, map1.get("in").toString(), startTime, endTime, (Integer) map1.get("invalueType"));
                MWZabbixAPIResult out = mwtpServerAPI.HistoryGetByTimeAndType(monitorServerId, map1.get("out").toString(), startTime, endTime, (Integer) map1.get("outvalueType"));
                List<HistoryValueDto> inData = ReportUtil.getValueData(in);
                List<HistoryValueDto> outData = ReportUtil.getValueData(out);
                if (inData.size() > 0 && outData.size() > 0) {

                    double inMax = inData.stream().mapToDouble(HistoryValueDto::getValue).max().getAsDouble();
                    double inMin = inData.stream().mapToDouble(HistoryValueDto::getValue).min().getAsDouble();
                    double inAvg = inData.stream().mapToDouble(HistoryValueDto::getValue).average().getAsDouble();

                    double outMax = outData.stream().mapToDouble(HistoryValueDto::getValue).max().getAsDouble();
                    double outMin = outData.stream().mapToDouble(HistoryValueDto::getValue).min().getAsDouble();
                    double outAvg = outData.stream().mapToDouble(HistoryValueDto::getValue).average().getAsDouble();

                    dto.setNetInBpsAvgValue(new BigDecimal(inAvg).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    dto.setNetInBpsMaxValue(new BigDecimal(inMax).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    dto.setNetInBpsMinValue(new BigDecimal(inMin).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    dto.setNetOutBpsAvgValue(new BigDecimal(outAvg).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    dto.setNetOutBpsMaxValue(new BigDecimal(outMax).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    dto.setNetOutBpsMinValue(new BigDecimal(outMin).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    return dto;
                }
            }
        }
        return null;
    }

    /**
     * 导入zabbix的网络接口数据到数据库
     *
     * @param mwTangibleassetsDTOS
     * @param startFrom
     * @param endTill
     * @param dateTime
     */
    public void inputNetWork(List<MwTangibleassetsTable> mwTangibleassetsDTOS, Long startFrom, Long endTill, Date dateTime) {
        List<NetWorkDto> allDayNetTrend = getNetTrends(mwTangibleassetsDTOS, startFrom, endTill);

        SolarTimeDto solarTimeDto = mwReportDao.selectTime(ReportBase.NETWORK.getId());
        String workStartTime = MWUtils.getSolarData(solarTimeDto.getStartHourTime(), solarTimeDto.getStartMinuteTime(), 0, MWDateConstant.NORM_DATETIME, -1);
        String workEndTime = MWUtils.getSolarData(solarTimeDto.getEndHourTime(), solarTimeDto.getEndMinuteTime(), 0, MWDateConstant.NORM_DATETIME, -1);
        Long workStartFrom = MWUtils.getDate(workStartTime, MWDateConstant.NORM_DATETIME);
        Long workEndTill = MWUtils.getDate(workEndTime, MWDateConstant.NORM_DATETIME);

        List<NetWorkDto> workNetTrend = getNetTrends(mwTangibleassetsDTOS, workStartFrom, workEndTill);

        String solarData = MWUtils.getSolarData(0, 0, 0, MWDateConstant.NORM_DATE, -1);//昨天的日期
        int count = mwReportDao.selectSolarDayCount(solarData);//判断昨天是不是休息日 0 是工作日  1是休息日

        if (count == 0) {
            /**
             * 工作日
             * 0-24小时的数据
             * 8-17（自定义时间段）点的数据
             */
            String workDayTableName = "mw_report_network_workday";
            String workDayWorkTimeTableName = "mw_report_network_workday_worktime";
            mwReportDao.insertReportNetWork(workDayTableName, dateTime, allDayNetTrend);
            mwReportDao.insertReportNetWork(workDayWorkTimeTableName, dateTime, workNetTrend);
        }
        String allDayTableName = "mw_report_network_allday";
        String allDayWorkTimeTableName = "mw_report_network_allday_worktime";
        mwReportDao.insertReportNetWork(allDayTableName, dateTime, allDayNetTrend);
        mwReportDao.insertReportNetWork(allDayWorkTimeTableName, dateTime, workNetTrend);
    }


}
