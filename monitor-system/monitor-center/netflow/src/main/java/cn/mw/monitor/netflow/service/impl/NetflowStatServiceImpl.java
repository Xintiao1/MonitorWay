package cn.mw.monitor.netflow.service.impl;

import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.netflow.clickhouse.entity.*;
import cn.mw.monitor.netflow.constant.FlowConstant;
import cn.mw.monitor.netflow.dao.*;
import cn.mw.monitor.netflow.entity.AppExpandPort;
import cn.mw.monitor.netflow.entity.ApplicationEntity;
import cn.mw.monitor.netflow.entity.IpGroupEntity;
import cn.mw.monitor.netflow.enums.ProtocolType;
import cn.mw.monitor.netflow.param.NetFlowRequestParam;
import cn.mw.monitor.netflow.service.NetflowStatService;
import cn.mw.monitor.netflow.service.clickhouse.ClickhouseUtil;
import cn.mw.monitor.netflow.util.DateUtil;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.assets.param.QueryTangAssetsParam;
import cn.mw.monitor.user.dto.GlobalUserInfo;
import cn.mw.monitor.user.service.MWUserService;
import cn.mw.monitor.util.NewUnits;
import cn.mw.monitor.util.UnitsUtil;
import cn.mwpaas.common.constant.DateConstant;
import cn.mwpaas.common.enums.DateUnitEnum;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.MathUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author guiquanwnag
 * @datetime 2023/8/1
 * @Description 新版流量分析实现类
 */
@Service
@Slf4j
public class NetflowStatServiceImpl implements NetflowStatService {

    /**
     * 获取TOP数据量
     */
    private final int NETFLOW_TOP_SIZE = 5;

    private final static int MAX_IN_SIZE = 500;

    /**
     * redis前缀
     */
    private static final String REDIS_PREFIX = "redis-netflow";

    /**
     * 默认的速率单位（KBps）
     */
    private final NewUnits DEFAULT_RATE_UNIT = NewUnits.KBPS;

    /**
     * 初始数据默认单位（Bps）
     */
    private final NewUnits INIT_RATE_UNIT = NewUnits.BPS;

    @Autowired
    public RedisTemplate<String, String> redisTemplate;

    @Resource
    private MWNetflowDao mwNetflowDao;

    @Autowired
    private MwAssetsManager assetsManager;

    @Autowired
    private ClickhouseUtil clickhouseUtil;

    @Resource
    private ApplicationManageDao applicationManageDao;

    @Resource
    private AppExpandPortManageDao portManageDao;

    @Resource
    private IpGroupManageDao ipGroupManageDao;

    @Resource
    private IpGroupNFAExpandManageDao nfaExpandManageDao;

    @Resource
    private IpGroupIPAMExpandManageDao ipamExpandManageDao;

    @Autowired
    private MWUserService mwUserService;


    /**
     * 获取流量监控结果
     *
     * @param requestParam 请求参数
     * @return
     */
    @Override
    public Reply browseResult(NetFlowRequestParam requestParam) {
        return getData(requestParam);
    }

    private Reply getData(NetFlowRequestParam requestParam) {
        long currentTime = System.currentTimeMillis();
        NetflowResult result = new NetflowResult();
        //判断是否有缓存
        String statKey = getStatRedisKey();
        if (redisTemplate.hasKey(statKey)) {
            result = JSON.parseObject(MWNetflowServiceImpl.uncompress(redisTemplate.opsForValue().get(statKey)), NetflowResult.class);
            return Reply.ok(result);
        }

        //如果是自定义时间，需要先校验时间
        if (5 == requestParam.getDateType()) {
            //先判断时间是否为1个月内
            if (requestParam.getStartTime() == null
                    || requestParam.getEndTime() == null
                    || requestParam.getStartTime().after(requestParam.getEndTime())) {
                return Reply.fail("时间格式不正确，开始时间需要在结束时间之前");
            }
            Calendar c = Calendar.getInstance();
            c.setTime(requestParam.getStartTime());
            c.add(Calendar.MONTH, 1);
            if (c.getTime().before(requestParam.getEndTime())) {
                return Reply.fail("最大时间仅支持一个月");
            }
            c.setTime(requestParam.getStartTime());
            c.add(Calendar.MINUTE, 5);
            if (c.getTime().after(requestParam.getEndTime())) {
                return Reply.fail("最小时间仅支持5分钟");
            }
        }
        //开始时间
        Date startTime;
        //结束时间
        Date endTime = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        switch (requestParam.getDateType()) {
            //最近一小时
            case 1:
                calendar.add(Calendar.HOUR_OF_DAY, -1);
                break;
            //最近一天
            case 2:
                calendar.add(Calendar.DAY_OF_YEAR, -1);
                break;
            //最近一周
            case 3:
                calendar.add(Calendar.WEEK_OF_YEAR, -1);
                break;
            //最近一月
            case 4:
                calendar.add(Calendar.MONTH, -1);
                break;
            //自定义
            case 5:
                endTime = requestParam.getEndTime();
                calendar.setTime(requestParam.getStartTime());
                break;
            //最近5分钟
            case 6:
                calendar.add(Calendar.MINUTE, -5);
                break;
            default:
                calendar.add(Calendar.HOUR_OF_DAY, -1);
                break;
        }
        startTime = calendar.getTime();


        //资产名称映射MAP
        Map<String, String> assetsNameMap = getAssetsNameMap();
        //先计算初始统计数据
        List<NetFlowTopData> initialStatList = getNetFlowList(startTime, endTime);

        //获取交互流量的排序数据
        List<NetFlowTopData> netFlowStatList = handleNetflowData(initialStatList);
        List<NetFlowTopData> netFlowTopList;
        if (netFlowStatList.size() > NETFLOW_TOP_SIZE) {
            netFlowTopList = netFlowStatList.subList(0, NETFLOW_TOP_SIZE);
        } else {
            netFlowTopList = new ArrayList<>(netFlowStatList);
        }
        //根据交互流量数据获取对应的折线图数据
        Map<String, NetflowChart> netflowChartMap = getTopLineChart(netFlowTopList,
                startTime, endTime);
        //更新资产IP对应的资产名称
        updateChartAssetName(netflowChartMap, assetsNameMap);

        //统计主机交互流量数据排序和TOPN折线图
        List<NetFlowTopData> hostStatList = statHostData(initialStatList);
        List<NetFlowTopData> hostTopList;
        if (hostStatList.size() > NETFLOW_TOP_SIZE) {
            hostTopList = hostStatList.subList(0, NETFLOW_TOP_SIZE);
        } else {
            hostTopList = new ArrayList<>(hostStatList);
        }
        //根据交互流量数据获取对应TOPN的折线图数据
        Map<String, NetflowChart> hostChartMap = getHostTopLineChart(hostTopList, startTime, endTime);
        //更新资产IP对应的资产名称
        updateHostChartAssetName(hostChartMap, assetsNameMap);

        //获取应用统计流量数据
        List<ApplicationEntity> appList = getAllAppList();
        Map<Integer, ApplicationEntity> appMap = appList.stream().collect(Collectors.toMap(ApplicationEntity::getId, obj -> obj));
        //获取结果数据(未进行收敛,因为需要考虑折线图的统计)(key:appId)
        Map<Integer, List<AppTopData>> recordAppMap = statAppTop(initialStatList, appList);
        //将数据收敛（key:app流量信息，value:对应的ip+port+协议的检索信息）
        Map<AppTopData, List<AppTopData>> statAppMap = handleAppMap(recordAppMap, appMap);
        Map<String, List<AppTopData>> searchAppIndexMap = new HashMap<>();
        for (AppTopData appTopData : statAppMap.keySet()) {
            searchAppIndexMap.put(getIndexKey(appTopData), statAppMap.get(appTopData));
        }
        List<AppTopData> appTopStatList = sortAndChangeAppList(new ArrayList<>(statAppMap.keySet()));
        //TOPN的流量数据
        List<AppTopData> appTopList;
        if (appTopStatList.size() > NETFLOW_TOP_SIZE) {
            appTopList = appTopStatList.subList(0, NETFLOW_TOP_SIZE);
        } else {
            appTopList = new ArrayList<>(appTopStatList);
        }
        //由于TOPN应用的IP+port+协议都不一样，无法统一获取折线图数据。所以需要分开获取
        List<NetflowChart> appChartList = new ArrayList<>();
        //如果用户未创建应用，则所有流量数据都会流入未知应用，这时候图形数据只需要根据未知应用统计
        if (appTopList.size() == 1 && NetflowStatService.UNKNOWN_APP_ID.equals(appTopList.get(0).getAppId())) {
            AppTopData appTopData = appTopList.get(0);
            NetflowChart appChart = getAppTopLineChart(new ArrayList<>(), startTime, endTime);
            appChart.setUnit(DEFAULT_RATE_UNIT.getUnits());
            appChart.setTitleName(appTopData.getAppName());
            appChartList.add(appChart);
        } else {
            for (AppTopData appTopData : appTopList) {
                NetflowChart appChart = getAppTopLineChart(searchAppIndexMap.get(getIndexKey(appTopData)), startTime, endTime);
                appChart.setUnit(DEFAULT_RATE_UNIT.getUnits());
                appChart.setTitleName(appTopData.getAppName());
                appChartList.add(appChart);
            }
        }

        //构建返回数据
        updateAssetsName(netFlowStatList, assetsNameMap);
        result.setNetFlowStatList(netFlowStatList);
        List<NetflowChart> list = new ArrayList<>(netflowChartMap.values());
        result.setNetFlowChartList(list);
        updateAssetsName(hostStatList, assetsNameMap);
        List<NetflowChart> chartList = new ArrayList<>(hostChartMap.values());
        result.setHostChartList(chartList);
        result.setHostStatList(hostStatList);
        result.setAppStatList(appTopStatList);
        result.setAppChartList(appChartList);
        result.setStartTime(DateUtil.format(startTime, DateConstant.NORM_DATETIME));
        result.setEndTime(DateUtil.format(endTime, DateConstant.NORM_DATETIME));
        //缓存数据
        return Reply.ok(result);
    }

    private void updateHostChartAssetName(Map<String, NetflowChart> hostChartMap, Map<String, String> assetsNameMap) {
        //进行资产名称修改
        for (String resultKey : hostChartMap.keySet()) {
            NetflowChart netflowChart = hostChartMap.get(resultKey);
            netflowChart.setTitleName(getAssetsName(resultKey, assetsNameMap));
        }
    }

    private NetflowChart getAppTopLineChart(List<AppTopData> appTopData, Date startTime, Date endTime) {
        NetflowChart appChart = new NetflowChart();
        List<NetflowChartResult> resultList = new ArrayList<>();
        long intervalMillis = getIntervalMillis(startTime, endTime);
        String[] tableNameArray = getTableName(startTime, endTime);
        String dateQuerySql = getDateQuerySql(startTime, endTime);
        String dateLimitSql = buildDateLimitSql(startTime, endTime);
        String ipPortQuerySql = getIpQuerySql(appTopData);
        String fromSql = getAppChartFromSql(tableNameArray, dateQuerySql, ipPortQuerySql,dateLimitSql);
        String execSql = String.format(FlowConstant.NEW_NETFLOW_CHART, startTime.getTime(), intervalMillis, fromSql);
        log.info("execSQL:" + execSql);
        resultList = clickhouseUtil.selectAllData(execSql, NetflowChartResult.class);
        appChart = handleAppChartList(resultList, startTime, endTime);
        return appChart;

    }

    /**
     * 处理APP的图形数据
     *
     * @param resultList 流量折线图流量初始数据
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return
     */
    private NetflowChart handleAppChartList(List<NetflowChartResult> resultList, Date startTime, Date endTime) {
        NetflowChart chart = new NetflowChart();
        List<NetflowChartDetail> chartDetailList = new ArrayList<>();
        List<NetflowChartDetail> emptyChartList = new ArrayList<>();
        Set<Integer> intervalSet = new HashSet<>();
        long intervalMillis = getIntervalMillis(startTime, endTime);
        //判断从开始时间到结束时间跨越多少时间间隔
        long maxTimeInterval = (endTime.getTime() - startTime.getTime()) / intervalMillis;
        NetflowChartDetail chartDetail;
        for (NetflowChartResult result : resultList) {
            //插入折线图数据
            chartDetail = new NetflowChartDetail();
            chartDetail.setTimeInterval(result.getTimeInterval());
            chartDetail.setValue(result.getSumBytes());
            chartDetailList.add(chartDetail);
            intervalSet.add(result.getTimeInterval());
        }
        //根据resultIntervalMap补全空数据的时间间隔
        NetflowChartDetail emptyDetail;
        for (int i = 0; i < maxTimeInterval; i++) {
            if (!intervalSet.contains(i)) {
                emptyDetail = new NetflowChartDetail();
                emptyDetail.setTimeInterval(i);
                emptyDetail.setValue(0);
                emptyDetail.setUnitByReal(DEFAULT_RATE_UNIT.getUnits());
                emptyChartList.add(emptyDetail);
            }
        }
        chartDetailList.addAll(emptyChartList);
        //进行数据排序，同时修改数据格式
        chart.setRealData(sortAndChangeUnit(chartDetailList, startTime, endTime));
        return chart;
    }


    private String getAppChartFromSql(String[] tableNameArray, String dateQuerySql, String ipPortQuerySql, String dateLimitSql) {
        StringBuffer stringBuffer = new StringBuffer();
        String tableName;
        for (int i = 0; i < tableNameArray.length; i++) {
            tableName = FlowConstant.CAP_DATABASE + FlowConstant.DATABASE_LINK + tableNameArray[i];
            stringBuffer.append(String.format(FlowConstant.NEW_NETFLOW_CHART_SEARCH, tableName, concatSql(FlowConstant.AND, dateQuerySql, ipPortQuerySql,dateLimitSql)));
            if (i != tableNameArray.length - 1) {
                stringBuffer.append(" union all ");
            }
        }
        return stringBuffer.toString();
    }

    private Map<Integer, List<AppTopData>> statAppTop(List<NetFlowTopData> initialStatList, List<ApplicationEntity> appList) {

        Map<Integer, List<AppTopData>> recordTopDataMap = new HashMap<>();
        for (ApplicationEntity app : appList) {
            //增加记录信息
            recordTopDataMap.put(app.getId(), new ArrayList<>());
        }
        //未知应用数据
        AppTopData unknownApp = geneUnknown();
        recordTopDataMap.put(unknownApp.getAppId(), new ArrayList<>());
        List<AppTopData> recordList;
        //是否是未知流量
        boolean isUnknown;
        AppTopData childTopData;
        AppTopData parentTopData;
        ProtocolType appProtocolType;
        for (NetFlowTopData data : initialStatList) {
            isUnknown = true;
            for (ApplicationEntity app : appList) {
                appProtocolType = ProtocolType.getByType(app.getProtocolType());
                if (appProtocolType == null) {
                    continue;
                }
                if (appProtocolType != ProtocolType.TCP && appProtocolType != ProtocolType.ALL){
                    continue;
                }
                //判断源端口是否命中应用端口列表（如果要目的端口也判断是否命中，则需要加checkPort(data.getDstPort(),app.getPortList())）
                if (checkPort(data.getSourcePort(), app.getPortList()) || checkPort(data.getDstPort(), app.getPortList())) {
                    recordList = recordTopDataMap.get(app.getId());
                    for (AppExpandPort expandPort : app.getPortList()) {
                        //判断流量入端口和流量出端口是否命中当前端口,判断IP地址是否命中应用的IP组信息
                        if ((checkPort(data.getSourcePort(), expandPort) || checkPort(data.getDstPort(), expandPort)) &&
                                checkAppIp(data.getSourceIp(), app, true) &&
                                checkAppIp(data.getDstIp(), app, false)) {
                            //将满足条件的数据整合到一起
                            parentTopData = getAppData(data);
                            parentTopData.setAppName(app.getApplicationName());
                            parentTopData.setAppId(app.getId());
                            //生成端口数据
                            childTopData = getAppData(data);
                            childTopData.setAppName(expandPort.getPortContent());
                            childTopData.setAppId(expandPort.getId());
                            List<AppTopData> list = new ArrayList<>();
                            list.add(childTopData);
                            parentTopData.setChildList(list);
                            recordList.add(parentTopData);
                            isUnknown = false;
                        }
                    }
                }
            }
            if (isUnknown) {
                recordList = recordTopDataMap.get(unknownApp.getAppId());
                parentTopData = getAppData(data);
                recordList.add(parentTopData);
            }
        }
        return recordTopDataMap;

    }

    private List<NetFlowTopData> statHostData(List<NetFlowTopData> initialStatList) {
        List<NetFlowTopData> hostList = new ArrayList<>();
        Map<String, Integer> hostMap = new HashMap<>();
        int index;
        String ip;
        NetFlowTopData hostData;
        for (NetFlowTopData data : initialStatList) {
            //计算源IP
            ip = data.getSourceIp();
            if (hostMap.containsKey(ip)) {
                index = hostMap.get(ip);
                hostData = hostList.get(index);
                hostData.setCompareData(hostData.getCompareData() + data.getCompareData());
            } else {
                hostData = new NetFlowTopData();
                hostData.setSourceIp(ip);
                hostData.setDstIp(ip);
                hostData.setCompareData(data.getCompareData());
                hostList.add(hostData);
                hostMap.put(ip, hostList.size() - 1);
            }
            //计算目标IP
            ip = data.getDstIp();
            if (hostMap.containsKey(ip)) {
                index = hostMap.get(ip);
                hostData = hostList.get(index);
                hostData.setCompareData(hostData.getCompareData() + data.getCompareData());
            } else {
                hostData = new NetFlowTopData();
                hostData.setSourceIp(ip);
                hostData.setDstIp(ip);
                hostData.setCompareData(data.getCompareData());
                hostList.add(hostData);
                hostMap.put(ip, hostList.size() - 1);
            }
        }
        //排序
        sortTopList(hostList);
        calcNetFlowPercent(hostList);
        //将数据进行计算
        Map<String, String> unitResult;
        for (NetFlowTopData data : hostList) {
            unitResult = UnitsUtil.getConvertedValue(new BigDecimal(data.getCompareData()), NewUnits.B.getUnits());
            data.setUnit(unitResult.get("units"));
            data.setSumData(Double.valueOf(unitResult.get("value")));
        }
        return hostList;
    }

    private void updateChartAssetName(Map<String, NetflowChart> netflowChartMap, Map<String, String> assetsNameMap) {
        //进行资产名称修改
        for (String resultKey : netflowChartMap.keySet()) {
            NetflowChart netflowChart = netflowChartMap.get(resultKey);
            String[] ipArray = resultKey.split(FlowConstant.REAL_PLUS);
            netflowChart.setTitleName(getAssetsName(ipArray[0], assetsNameMap)
                    + " to " + getAssetsName(ipArray[1], assetsNameMap));
        }
    }

    private Map<String, NetflowChart> getTopLineChart(List<NetFlowTopData> netFlowTopList, Date startTime, Date endTime) {
        Map<String, NetflowChart> resultMap = new HashMap<>();
        List<NetflowChartResult> resultList = new ArrayList<>();
        long intervalMillis = getIntervalMillis(startTime, endTime);
        String[] tableNameArray = getTableName(startTime, endTime);
        String dateQuerySql = getDateQuerySql(startTime, endTime);
        String dateLimitSql = buildDateLimitSql(startTime,endTime);
        String ipPortQuerySql = getIpQuerySql(netFlowTopList);
        String fromSql = getChartFromSql(tableNameArray, dateQuerySql, ipPortQuerySql,dateLimitSql);
        String execSql = String.format(FlowConstant.NEW_NETFLOW_CHART, startTime.getTime(), intervalMillis, fromSql);
        log.info("execSQL:" + execSql);
        resultList = clickhouseUtil.selectAllData(execSql, NetflowChartResult.class);
        resultMap = handleChartList(resultList, startTime, endTime);
        return resultMap;
    }

    private Map<String, NetflowChart> getHostTopLineChart(List<NetFlowTopData> netFlowTopList, Date startTime, Date endTime) {
        Map<String, NetflowChart> resultMap = new HashMap<>();
        List<NetflowChartResult> resultList = new ArrayList<>();
        Set<String> hostSet = netFlowTopList.stream().map(NetFlowTopData::getSourceIp).collect(Collectors.toSet());
        long intervalMillis = getIntervalMillis(startTime, endTime);
        String[] tableNameArray = getTableName(startTime, endTime);
        String dateQuerySql = getDateQuerySql(startTime, endTime);
        String ipPortQuerySql = getHostIpQuerySql(netFlowTopList);
        String dateLimitSql = buildDateLimitSql(startTime,endTime);
        String fromSql = getChartFromSql(tableNameArray, dateQuerySql, ipPortQuerySql,dateLimitSql);
        String execSql = String.format(FlowConstant.NEW_NETFLOW_CHART, startTime.getTime(), intervalMillis, fromSql);
        log.info("execSQL:" + execSql);
        resultList = clickhouseUtil.selectAllData(execSql, NetflowChartResult.class);
        resultMap = handleHostChartList(hostSet,resultList, startTime, endTime);
        return resultMap;
    }

    private Map<String, NetflowChart> handleHostChartList(Set<String> hostSet, List<NetflowChartResult> resultList, Date startTime, Date endTime) {
        Map<String, NetflowChart> resultMap = new HashMap<>();
        Map<String, Set<Integer>> resultIntervalMap = new HashMap<>();
        Map<String,Map<Integer,Double>> ipChartMap = new HashMap<>();
        Map<String,Map<Integer,Integer>> indexMap = new HashMap<>();
        Map<Integer,Double> timeIntervalMap;
        Map<Integer,Integer> timeIntervalIndexMap;
        List<NetflowChartDetail> chartDetailList = new ArrayList<>();
        Set<Integer> intervalSet = new HashSet<>();
        long intervalMillis = getIntervalMillis(startTime, endTime);
        //判断从开始时间到结束时间跨越多少时间间隔
        long maxTimeInterval = (endTime.getTime() - startTime.getTime()) / intervalMillis;
        String key;
        NetflowChart chart;
        NetflowChartDetail chartDetail;
        int timeInterval;
        double sumValue;
        int listIndex;
        boolean isDest = false;
        for (String ip : hostSet) {
            chart = new NetflowChart();
            resultMap.put(ip, chart);
            ipChartMap.put(ip,new HashMap<>());
            indexMap.put(ip,new HashMap<>());
        }
        for (NetflowChartResult result : resultList) {
            key = result.getSourceIp();
            if (resultMap.containsKey(key)) {
                chart = resultMap.get(key);
            } else {
                key = result.getDstIp();
                chart = resultMap.get(key);
                isDest = true;
            }
            if (chart == null){continue;}
            timeIntervalMap = ipChartMap.get(key);
            timeIntervalIndexMap = indexMap.get(key);
            if (CollectionUtils.isNotEmpty(chart.getRealData())) {
                chartDetailList = chart.getRealData();
            } else {
                chartDetailList = new ArrayList<>();
                chart.setRealData(chartDetailList);
            }

            timeInterval = result.getTimeInterval();
            if (timeIntervalMap.containsKey(timeInterval)){
                sumValue = timeIntervalMap.get(timeInterval);
                listIndex = timeIntervalIndexMap.get(timeInterval);
                chartDetail = chartDetailList.get(listIndex);
                sumValue += result.getSumBytes();
                chartDetail.setValue(sumValue);
            }else {
                //插入折线图数据
                sumValue = result.getSumBytes();
                chartDetail = new NetflowChartDetail();
                chartDetail.setTimeInterval(timeInterval);
                chartDetail.setValue(sumValue);
                chartDetailList.add(chartDetail);
                timeIntervalMap.put(timeInterval,sumValue);
                timeIntervalIndexMap.put(timeInterval, (chartDetailList.size()-1));
            }

            if (resultIntervalMap.containsKey(key)) {
                intervalSet = resultIntervalMap.get(key);
            } else {
                intervalSet = new HashSet<>();
                resultIntervalMap.put(key, intervalSet);
            }
            intervalSet.add(timeInterval);

            //如果destIP是前5，需要考虑再累加一次
            if (!isDest){
                key = result.getDstIp();
                chart = resultMap.get(key);
                if (chart == null){continue;}
                timeIntervalMap = ipChartMap.get(key);
                timeIntervalIndexMap = indexMap.get(key);
                if (CollectionUtils.isNotEmpty(chart.getRealData())) {
                    chartDetailList = chart.getRealData();
                } else {
                    chartDetailList = new ArrayList<>();
                    chart.setRealData(chartDetailList);
                }

                timeInterval = result.getTimeInterval();
                if (timeIntervalMap.containsKey(timeInterval)){
                    sumValue = timeIntervalMap.get(timeInterval);
                    listIndex = timeIntervalIndexMap.get(timeInterval);
                    chartDetail = chartDetailList.get(listIndex);
                    sumValue += result.getSumBytes();
                    chartDetail.setValue(sumValue);
                }else {
                    //插入折线图数据
                    sumValue = result.getSumBytes();
                    chartDetail = new NetflowChartDetail();
                    chartDetail.setTimeInterval(timeInterval);
                    chartDetail.setValue(sumValue);
                    chartDetailList.add(chartDetail);
                    timeIntervalMap.put(timeInterval,sumValue);
                    timeIntervalIndexMap.put(timeInterval, (chartDetailList.size()-1));
                }

                if (resultIntervalMap.containsKey(key)) {
                    intervalSet = resultIntervalMap.get(key);
                } else {
                    intervalSet = new HashSet<>();
                    resultIntervalMap.put(key, intervalSet);
                }
                intervalSet.add(timeInterval);
            }
            isDest = false;
        }
        //根据resultIntervalMap补全空数据的时间间隔
        for (String resultKey : resultMap.keySet()) {
            NetflowChart netflowChart = resultMap.get(resultKey);
            Set<Integer> set = resultIntervalMap.get(resultKey);
            List<NetflowChartDetail> emptyChartList = new ArrayList<>();
            for (int i = 0; i < maxTimeInterval; i++) {
                if (!set.contains(i)) {
                    NetflowChartDetail emptyDetail = new NetflowChartDetail();
                    emptyDetail.setTimeInterval(i);
                    emptyDetail.setValue(0);
                    emptyDetail.setUnitByReal(DEFAULT_RATE_UNIT.getUnits());
                    emptyChartList.add(emptyDetail);
                }
            }
            netflowChart.getRealData().addAll(emptyChartList);
        }
        //进行数据排序，同时修改数据格式
        for (String resultKey : resultMap.keySet()) {
            NetflowChart netflowChart = resultMap.get(resultKey);
            netflowChart.setRealData(sortAndChangeUnit(netflowChart.getRealData(), startTime, endTime));
        }
        return resultMap;
    }

    private String getHostIpQuerySql(List<NetFlowTopData> netFlowTopList) {
        if (CollectionUtils.isEmpty(netFlowTopList)) {
            return FlowConstant.EMPTY_STRING;
        }
        NetFlowTopData topData;
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(" (");
        for (int i = 0; i < netFlowTopList.size(); i++) {
            topData = netFlowTopList.get(i);
            stringBuffer.append("(");
            stringBuffer.append(FlowConstant.SPACE).append("srcIp").append(FlowConstant.EQUAL).append(getStringSql(topData.getSourceIp()));
            stringBuffer.append(FlowConstant.SPACE).append(FlowConstant.OR);
            stringBuffer.append(FlowConstant.SPACE).append("destIp").append(FlowConstant.EQUAL).append(getStringSql(topData.getDstIp()));
            stringBuffer.append(")");
            if (i != (netFlowTopList.size() - 1)) {
                stringBuffer.append(FlowConstant.SPACE).append(FlowConstant.OR);
            }
        }
        stringBuffer.append(")");
        return stringBuffer.toString();
    }

    private Map<String, NetflowChart> handleChartList(List<NetflowChartResult> resultList, Date startTime, Date endTime) {
        Map<String, NetflowChart> resultMap = new HashMap<>();
        Map<String, Set<Integer>> resultIntervalMap = new HashMap<>();
        List<NetflowChartDetail> chartDetailList = new ArrayList<>();
        Set<Integer> intervalSet = new HashSet<>();
        long intervalMillis = getIntervalMillis(startTime, endTime);
        //判断从开始时间到结束时间跨越多少时间间隔
        long maxTimeInterval = (endTime.getTime() - startTime.getTime()) / intervalMillis;
        String key;
        NetflowChart chart;
        NetflowChartDetail chartDetail;
        for (NetflowChartResult result : resultList) {
            key = result.getSourceIp() + FlowConstant.PLUS + result.getDstIp();
            if (resultMap.containsKey(key)) {
                chart = resultMap.get(key);
            } else {
                chart = new NetflowChart();
                resultMap.put(key, chart);
            }
            if (CollectionUtils.isNotEmpty(chart.getRealData())) {
                chartDetailList = chart.getRealData();
            } else {
                chartDetailList = new ArrayList<>();
                chart.setRealData(chartDetailList);
            }
            //插入折线图数据
            chartDetail = new NetflowChartDetail();
            chartDetail.setTimeInterval(result.getTimeInterval());
            chartDetail.setValue(result.getSumBytes());
            chartDetailList.add(chartDetail);
            if (resultIntervalMap.containsKey(key)) {
                intervalSet = resultIntervalMap.get(key);
            } else {
                intervalSet = new HashSet<>();
                resultIntervalMap.put(key, intervalSet);
            }
            intervalSet.add(result.getTimeInterval());
        }
        //根据resultIntervalMap补全空数据的时间间隔
        for (String resultKey : resultMap.keySet()) {
            NetflowChart netflowChart = resultMap.get(resultKey);
            Set<Integer> set = resultIntervalMap.get(resultKey);
            List<NetflowChartDetail> emptyChartList = new ArrayList<>();
            for (int i = 0; i < maxTimeInterval; i++) {
                if (!set.contains(i)) {
                    NetflowChartDetail emptyDetail = new NetflowChartDetail();
                    emptyDetail.setTimeInterval(i);
                    emptyDetail.setValue(0);
                    emptyDetail.setUnitByReal(DEFAULT_RATE_UNIT.getUnits());
                    emptyChartList.add(emptyDetail);
                }
            }
            netflowChart.getRealData().addAll(emptyChartList);
        }
        //进行数据排序，同时修改数据格式
        for (String resultKey : resultMap.keySet()) {
            NetflowChart netflowChart = resultMap.get(resultKey);
            netflowChart.setRealData(sortAndChangeUnit(netflowChart.getRealData(), startTime, endTime));
        }
        return resultMap;
    }

    private List<NetflowChartDetail> sortAndChangeUnit(List<NetflowChartDetail> detailList, Date startTime, Date endTime) {
        long intervalMillis = getIntervalMillis(startTime, endTime);
        long intervalSeconds = intervalMillis / DateUnitEnum.SECOND.getMillis();
        //判断从开始时间到结束时间跨越多少时间间隔
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startTime);
        //sort
        List<NetflowChartDetail> sortedList = detailList.stream()
                .sorted(Comparator.comparing(NetflowChartDetail::getTimeInterval)).collect(Collectors.toList());
        double rate = 0;
        Map<String, String> unitResult;
        for (NetflowChartDetail chartDetail : sortedList) {
            //进行数据格式转换
            if (chartDetail.getValue() > 0) {
                rate = chartDetail.getValue() / intervalSeconds;
                unitResult = UnitsUtil.getConvertedValue(new BigDecimal(rate), DEFAULT_RATE_UNIT.getUnits(), INIT_RATE_UNIT.getUnits());
                chartDetail.setUnitByReal(DEFAULT_RATE_UNIT.getUnits());
                chartDetail.setValue(Double.parseDouble(unitResult.get("value")));
            }
            //时间转换
            calendar.add(Calendar.MILLISECOND, (int) intervalMillis);
            chartDetail.setDateTime(DateUtil.format(calendar.getTime(), DateConstant.NORM_DATETIME));
        }
        return sortedList;
    }

    private String getChartFromSql(String[] tableNameArray, String dateQuerySql, String ipPortQuerySql ,String dateLimitSql) {
        StringBuffer stringBuffer = new StringBuffer();
        String tableName;
        for (int i = 0; i < tableNameArray.length; i++) {
            tableName = FlowConstant.CAP_DATABASE + FlowConstant.DATABASE_LINK + tableNameArray[i];
            stringBuffer.append(String.format(FlowConstant.NEW_NETFLOW_CHART_SEARCH, tableName, concatSql(FlowConstant.AND, dateQuerySql, ipPortQuerySql,dateLimitSql)));
            if (i != tableNameArray.length - 1) {
                stringBuffer.append(" union all ");
            }
        }
        return stringBuffer.toString();
    }

    private String getIpQuerySql(List<? extends NetFlowTopData> netFlowTopList) {
        if (CollectionUtils.isEmpty(netFlowTopList)) {
            return FlowConstant.EMPTY_STRING;
        }
        NetFlowTopData topData;
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(" (");
        for (int i = 0; i < netFlowTopList.size(); i++) {
            topData = netFlowTopList.get(i);
            stringBuffer.append("(");
            stringBuffer.append(FlowConstant.SPACE).append("srcIp").append(FlowConstant.EQUAL).append(getStringSql(topData.getSourceIp()));
            stringBuffer.append(FlowConstant.SPACE).append(FlowConstant.AND);
            stringBuffer.append(FlowConstant.SPACE).append("destIp").append(FlowConstant.EQUAL).append(getStringSql(topData.getDstIp()));
            if (StringUtils.isNotEmpty(topData.getSourcePort())) {
                stringBuffer.append(FlowConstant.SPACE).append(FlowConstant.AND);
                stringBuffer.append(FlowConstant.SPACE).append("srcPort").append(FlowConstant.EQUAL).append(topData.getSourcePort());
            }
            if (StringUtils.isNotEmpty(topData.getDstPort())) {
                stringBuffer.append(FlowConstant.SPACE).append(FlowConstant.AND);
                stringBuffer.append(FlowConstant.SPACE).append("destPort").append(FlowConstant.EQUAL).append(topData.getDstPort());
            }
            stringBuffer.append(")");
            if (i != (netFlowTopList.size() - 1)) {
                stringBuffer.append(FlowConstant.SPACE).append(FlowConstant.OR);
            }
        }
        stringBuffer.append(")");
        return stringBuffer.toString();
    }

    private String getStringSql(String str) {
        return "\'" + str + "\'";
    }

    private List<NetFlowTopData> handleNetflowData(List<NetFlowTopData> initialStatList) {
        List<NetFlowTopData> statList = new ArrayList<>();
        //根据初始数据根据IP地址统计流量数据
        Map<String, Integer> keyMap = new HashMap<>();
        NetFlowTopData topData;
        String key;
        for (NetFlowTopData data : initialStatList) {
            key = data.getSourceIp() + FlowConstant.PLUS + data.getDstIp();
            if (keyMap.containsKey(key)) {
                topData = statList.get(keyMap.get(key));
                topData.setSumData(topData.getSumData() + data.getCompareData());
                topData.setCompareData(topData.getCompareData() + data.getCompareData());
            } else {
                topData = new NetFlowTopData();
                topData.setSourceIp(data.getSourceIp());
                topData.setDstIp(data.getDstIp());
                topData.setSumData(data.getCompareData());
                topData.setCompareData(data.getCompareData());
                statList.add(topData);
                keyMap.put(key, statList.size() - 1);
            }
        }
        //先排序
        sortTopList(statList);
        //再计算占比
        calcNetFlowPercent(statList);
        //将单位转换成合适的单位
        calcNetFlowUnit(statList);
        return statList;
    }

    /**
     * 获取缓存的key
     *
     * @return
     */
    private String getStatRedisKey() {
        return REDIS_PREFIX + "-stat-cache" + "-clickhouse" ;
    }

    /**
     * 获取资产和IP对应的映射关系
     *
     * @return
     */
    private Map<String, String> getAssetsNameMap() {
        GlobalUserInfo userInfo = mwUserService.getGlobalUser();
        Map<String, String> map = new HashMap<>();
        QueryTangAssetsParam param = new QueryTangAssetsParam();
        param.setPageSize(Integer.MAX_VALUE);
        param.setIsQueryAssetsState(false);
        param.setUserId(userInfo.getUserId());
        List<MwTangibleassetsTable> assetList = assetsManager.getAssetsTable(param);
        for (MwTangibleassetsTable asset : assetList) {
            map.put(asset.getInBandIp(), asset.getAssetsName());
        }
        return map;
    }


    /**
     * 根据流量出入情况获取流量分析列表
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return
     */
    private List<NetFlowTopData> getNetFlowList(Date startTime, Date endTime) {
        String[] tableNameArray = getTableName(startTime, endTime);
        String dateQuerySql = getDateQuerySql(startTime, endTime);
        String dateLimitSql = buildDateLimitSql(startTime,endTime);
        String fromSql = getFromSql(tableNameArray, dateQuerySql,dateLimitSql);
        String execSql = String.format(FlowConstant.NEW_STAT_NETFLOW_TOP_ALL, fromSql);
        log.info("execSQL:" + execSql);
        List<NetFlowTopData> dataList = clickhouseUtil.selectAllData(execSql, NetFlowTopData.class);
        return dataList;
    }

    private String getFromSql(String[] tableNameArray, String dateQuerySql, String dateLimitSql) {
        StringBuffer stringBuffer = new StringBuffer();
        String tableName;
        for (int i = 0; i < tableNameArray.length; i++) {
            tableName = FlowConstant.CAP_DATABASE + FlowConstant.DATABASE_LINK + tableNameArray[i];
            stringBuffer.append(String.format(FlowConstant.NEW_NETFLOW_SEARCH, tableName, concatSql(FlowConstant.AND,dateLimitSql,dateQuerySql)));
            if (i != tableNameArray.length - 1) {
                stringBuffer.append(" union all ");
            }
        }
        return stringBuffer.toString();
    }

    /**
     * 获取clickhouse数据库的表名
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return
     */
    private String[] getTableName(Date startTime, Date endTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startTime);
        int betweenMonth = calculateMonthsDifference(startTime, endTime);
        betweenMonth++;
        String[] tableArray = new String[betweenMonth];
        for (int i = 0; i < betweenMonth; i++) {
            tableArray[i] = FlowConstant.CIICKHOUSE_INDEX_PREFIX + FlowConstant.TABLE_SEP + DateUtil.format(calendar.getTime(), FlowConstant.MONTH_FORMAT);
            calendar.add(Calendar.MONTH, 1);
        }
        return tableArray;
    }

    /**
     * 计算相差月份
     *
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @return
     */
    private int calculateMonthsDifference(Date startDate, Date endDate) {
        // 将日期转换为 Calendar 对象
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(startDate);
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(endDate);
        // 计算日期差距
        int yearsDifference = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
        int monthsDifference = endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH);
        // 将年份转换为月份
        return yearsDifference * 12 + monthsDifference;
    }

    /**
     * 获取时间筛选sql
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return
     */
    private String getDateQuerySql(Date startTime, Date endTime) {
        return String.format("( createTime > %d and createTime < %d )", startTime.getTime(), endTime.getTime());
    }

    /**
     * 给流量监控集合机型排序(从大到小排)
     *
     * @param topList
     */
    private void sortTopList(List<? extends NetFlowTopData> topList) {
        Collections.sort(topList, new Comparator<NetFlowTopData>() {
            @Override
            public int compare(NetFlowTopData o1, NetFlowTopData o2) {
                if (o1.getCompareData() - o2.getCompareData() > 0) {
                    return -1;
                } else if (o1.getCompareData() - o2.getCompareData() < 0) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
    }

    /**
     * 计算流量数据占比
     *
     * @param statTopList 流量监控数据
     */
    private void calcNetFlowPercent(List<NetFlowTopData> statTopList) {
        double allData = 0;
        for (NetFlowTopData data : statTopList) {
            allData += data.getCompareData();
        }
        for (NetFlowTopData data : statTopList) {
            data.setNetFlowPercent(MathUtils.getPercentValue(data.getCompareData(), allData));
            data.setNetFlowPercentString(MathUtils.getPercent(data.getCompareData(), allData));
        }
    }

    /**
     * 计算数据单位
     *
     * @param list 数据
     */
    private void calcNetFlowUnit(List<? extends NetFlowTopData> list) {
        Map<String, String> result;
        for (NetFlowTopData data : list) {
            result = UnitsUtil.getConvertedValue(new BigDecimal(data.getCompareData()), NewUnits.B.getUnits());
            data.setUnit(result.get("units"));
            data.setSumData(Double.parseDouble(result.get("value")));
        }
    }

    /**
     * 根据开始时间和结束时间获取时间间隔（毫秒级）
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return
     */
    private long getIntervalMillis(Date startTime, Date endTime) {
        long intervalTime = endTime.getTime() - startTime.getTime();
        //如果开始时间和结束时间间隔小于等于1分钟，间隔1秒
        if (intervalTime <= DateUnitEnum.getMillis(1, DateUnitEnum.MINUTE)) {
            return DateUnitEnum.getMillis(1, DateUnitEnum.SECOND);
            //大于1分钟，小于等于30分钟，间隔30秒
        } else if (intervalTime <= DateUnitEnum.getMillis(30, DateUnitEnum.MINUTE)) {
            return DateUnitEnum.getMillis(30, DateUnitEnum.SECOND);
            //大于30分钟，小于等于一个小时，间隔1分钟
        } else if (intervalTime <= DateUnitEnum.getMillis(1, DateUnitEnum.HOUR)) {
            return DateUnitEnum.getMillis(1, DateUnitEnum.MINUTE);
            //大于一个小时，小于等于6个小时，间隔5分钟
        } else if (intervalTime <= DateUnitEnum.getMillis(6, DateUnitEnum.HOUR)) {
            return DateUnitEnum.getMillis(5, DateUnitEnum.MINUTE);
            //大于6个小时，小于等于12个小时，间隔十分钟
        } else if (intervalTime <= DateUnitEnum.getMillis(12, DateUnitEnum.HOUR)) {
            return DateUnitEnum.getMillis(10, DateUnitEnum.MINUTE);
            //大于12个小时，小于等于1天，间隔三十分钟
        } else if (intervalTime <= DateUnitEnum.getMillis(1, DateUnitEnum.DAY)) {
            return DateUnitEnum.getMillis(30, DateUnitEnum.MINUTE);
            //大于一天，小于等于4天，间隔1小时
        } else if (intervalTime <= DateUnitEnum.getMillis(4, DateUnitEnum.DAY)) {
            return DateUnitEnum.getMillis(1, DateUnitEnum.HOUR);
            //大于4天，小于等于一个月，间隔12个小时
        } else if (intervalTime <= DateUnitEnum.getMillis(1, DateUnitEnum.MONTH)) {
            return DateUnitEnum.getMillis(12, DateUnitEnum.HOUR);
            //大于一个月，小于等于一年，间隔一天
        } else if (intervalTime <= DateUnitEnum.getMillis(12, DateUnitEnum.MONTH)) {
            return DateUnitEnum.getMillis(1, DateUnitEnum.DAY);
            //大于一年，间隔1周
        } else {
            return DateUnitEnum.getMillis(1, DateUnitEnum.WEEK);
        }
    }


    /**
     * 将多个sql拼接起来
     *
     * @param operator 操作符  AND  OR
     * @param sqls     语句
     * @return
     */
    private String concatSql(String operator, String... sqls) {
        if (sqls == null || sqls.length == 0) {
            return FlowConstant.EMPTY_STRING;
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(" (");
        String sql;
        for (int i = 0; i < sqls.length; i++) {
            sql = sqls[i];
            if (StringUtils.isEmpty(sql)) {
                continue;
            }
            stringBuffer.append(FlowConstant.SPACE).append(sql).append(FlowConstant.SPACE);
            stringBuffer.append(operator);
        }
        //去除最后一位运算符
        if (stringBuffer.toString().endsWith(operator)) {
            stringBuffer.setLength(stringBuffer.length() - operator.length());
        }
        stringBuffer.append(")");
        return stringBuffer.toString();
    }

    /**
     * 根据IP获取资产名称
     *
     * @param ip  IP
     * @param map 资产名称map
     * @return
     */
    private String getAssetsName(String ip, Map<String, String> map) {
        if (map.containsKey(ip)) {
            return map.get(ip);
        }
        return ip;
    }

    /**
     * 获取所有应用信息数据
     *
     * @return
     */
    private List<ApplicationEntity> getAllAppList() {
        //获取应用基础信息
        QueryWrapper<ApplicationEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("delete_flag", false);
        List<ApplicationEntity> appList = applicationManageDao.selectList(wrapper);
        //获取端口列表
        QueryWrapper<AppExpandPort> portWrapper;
        List<Integer> parentIdList = new ArrayList<>();
        List<AppExpandPort> allPortList = new ArrayList<>();
        Map<Integer, List<AppExpandPort>> appPortMap = new HashMap<>();
        for (ApplicationEntity application : appList) {
            parentIdList.add(application.getId());
        }
        List<List<Integer>> parentIdLists = Lists.partition(parentIdList, MAX_IN_SIZE);
        for (List<Integer> lists : parentIdLists) {
            portWrapper = new QueryWrapper<>();
            portWrapper.in("parent_id", lists);
            List<AppExpandPort> portList = portManageDao.selectList(portWrapper);
            allPortList.addAll(portList);
        }
        int parentId;
        List<AppExpandPort> appPortList;
        for (AppExpandPort port : allPortList) {
            parentId = port.getParentId();
            if (appPortMap.containsKey(parentId)) {
                appPortList = appPortMap.get(parentId);
                appPortList.add(port);
            } else {
                appPortList = new ArrayList<>();
                appPortList.add(port);
                appPortMap.put(parentId, appPortList);
            }
        }
        for (ApplicationEntity app : appList) {
            app.setPortList(appPortMap.get(app.getId()));
        }
        return appList;
    }

    /**
     * 创建未知应用
     *
     * @return
     */
    private AppTopData geneUnknown() {
        AppTopData unknownApp = new AppTopData();
        unknownApp.setAppName(NetflowStatService.UNKNOWN_APP_NAME);
        unknownApp.setAppId(NetflowStatService.UNKNOWN_APP_ID);
        //增加流量
        unknownApp.setInData(0D);
        unknownApp.setInPackage(0);
        unknownApp.setOutData(0D);
        unknownApp.setOutPackage(0);
        unknownApp.setSumData(0D);
        unknownApp.setSumPackage(0);
        unknownApp.setCompareData(0D);
        unknownApp.setComparePackage(0);
        return unknownApp;
    }

    /**
     * 判断端口列表是否包含该端口
     *
     * @param port     端口
     * @param portList 端口列表
     * @return
     */
    private boolean checkPort(String port, List<AppExpandPort> portList) {
        if (CollectionUtils.isEmpty(portList)) {
            return false;
        }
        String[] portArr;
        try {
            for (AppExpandPort expandPort : portList) {
                if (expandPort.getPortContent().contains(FlowConstant.NETFLOW_SEP)) {
                    portArr = expandPort.getPortContent().split(FlowConstant.NETFLOW_SEP);
                    if (StringUtils.isNotEmpty(portArr[0]) && StringUtils.isNotEmpty(portArr[1]) &&
                            (Integer.parseInt(portArr[0]) <= Integer.parseInt(port)) &&
                            (Integer.parseInt(portArr[1]) >= Integer.parseInt(port))) {
                        return true;
                    }
                } else {
                    if (expandPort.getPortContent().equals(port)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            log.error("判断端口列表是否包含该端口出现错误", e);
        }
        return false;
    }

    /**
     * 判断单个端口信息是否包含该端口
     *
     * @param port
     * @param expandPort
     * @return
     */
    private boolean checkPort(String port, AppExpandPort expandPort) {
        List<AppExpandPort> list = new ArrayList();
        list.add(expandPort);
        return checkPort(port, list);
    }

    /**
     * 检查ip是否命中应用的IP地址组
     *
     * @param ip   IP
     * @param app  应用信息
     * @param isIn true：入IP  false:出IP
     * @return
     */
    private boolean checkAppIp(String ip, ApplicationEntity app, boolean isIn) {
        List<String> list = new ArrayList<>();
        IpGroupEntity ipGroup;
        boolean result = false;
        if (isIn) {
            if (app.getSourceIpId() == 0) {
                return true;
            }
            ipGroup = ipGroupManageDao.selectById(app.getSourceIpId());
            if (ipGroup == null || ipGroup.getDeleteFlag()) {
                return false;
            }
        } else {
            if (app.getDestIpId() == 0) {
                return true;
            }
            ipGroup = ipGroupManageDao.selectById(app.getDestIpId());
            if (ipGroup == null || ipGroup.getDeleteFlag()) {
                return false;
            }
        }
        if (ipGroup.getAddType() == 1) {
            list = nfaExpandManageDao.getIpGroupList(ipGroup.getId());
            for (String ips : list) {
                if (ips.contains("/")) {
                    if (isInRange(ip, ips)) {
                        return true;
                    }
                } else if (ips.contains("-")) {
                    if (isInPhase(ip, ips)) {
                        return true;
                    }

                } else {
                    if (isInList(ip, Arrays.asList(ips.split(",")))) {
                        return true;
                    }
                }
            }
        } else if (ipGroup.getAddType() == 2) {
            list = ipamExpandManageDao.getIpGroupList(ipGroup.getId());
            for (String ips : list) {
                if (isInPhase(ip, ips)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断IP是否在指定网段内
     *
     * @param ip   ip
     * @param cidr 指定网段
     * @return
     */
    private boolean isInPhase(String ip, String cidr) {
        try {
            String[] ips = ip.split("\\.");
            int ipAddr = (Integer.parseInt(ips[0]) << 24) | (Integer.parseInt(ips[1]) << 16) |
                    (Integer.parseInt(ips[2]) << 8) | Integer.parseInt(ips[3]);
            int type = Integer.parseInt(cidr.replaceAll(".*/", ""));
            int mask = 0xFFFFFFFF << (32 - type);
            String cidrIp = cidr.replaceAll("/.*", "");
            String[] cidrIps = cidrIp.split("\\.");
            int cidrIpAddr = (Integer.parseInt(cidrIps[0]) << 24) | (Integer.parseInt(cidrIps[1]) << 16) |
                    (Integer.parseInt(cidrIps[2]) << 8) | Integer.parseInt(cidrIps[3]);
            return (ipAddr & mask) == (cidrIpAddr & mask);
        } catch (Exception e) {
            log.error("判断IP是否在指定网段内失败", e);
            return false;
        }
    }

    /**
     * 判断IP是否命中IP列表
     *
     * @param ip     ip
     * @param ipList IP列表
     * @return
     */
    private boolean isInList(String ip, List<String> ipList) {
        Set<String> set = new HashSet<>(ipList);
        return set.contains(ip);
    }

    /**
     * 判断IP是否命中IP地址段
     *
     * @param ip      ip
     * @param ipPhase IP地址段
     * @return
     */
    private boolean isInRange(String ip, String ipPhase) {
        try {
            ipPhase = ipPhase.trim();
            ip = ip.trim();
            final String REGX_IP = "((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|\\d)";
            final String REGX_IPB = REGX_IP + "\\-" + REGX_IP;
            if (!ipPhase.matches(REGX_IPB) || !ip.matches(REGX_IP)) {
                return false;
            }

            int idx = ipPhase.indexOf('-');
            String[] sips = ipPhase.substring(0, idx).split("\\.");
            String[] sipe = ipPhase.substring(idx + 1).split("\\.");
            String[] sipt = ip.split("\\.");
            long ips = 0L, ipe = 0L, ipt = 0L;
            for (int i = 0; i < 4; ++i) {
                ips = ips << 8 | Integer.parseInt(sips[i]);
                ipe = ipe << 8 | Integer.parseInt(sipe[i]);
                ipt = ipt << 8 | Integer.parseInt(sipt[i]);
            }
            if (ips > ipe) {
                long t = ips;
                ips = ipe;
                ipe = t;
            }
            return ips <= ipt && ipt <= ipe;
        } catch (Exception e) {
            log.error("判断IP是否命中IP地址段失败", e);
            return false;
        }
    }

    /**
     * 生成未收敛数据
     *
     * @param data 交互流量数据
     * @return
     */
    private AppTopData getAppData(NetFlowTopData data) {
        AppTopData record = new AppTopData();
        record.setSourceIp(data.getSourceIp());
        record.setDstIp(data.getDstIp());
        record.setSourcePort(data.getSourcePort());
        record.setDstPort(data.getDstPort());
        record.setProtocol(data.getProtocol());
        record.setInData(data.getInData());
        record.setInPackage(data.getInPackage());
        record.setOutData(data.getOutData());
        record.setOutPackage(data.getOutPackage());
        record.setSumData(data.getCompareData());
        record.setSumPackage(data.getComparePackage());
        record.setCompareData(data.getCompareData());
        record.setComparePackage(data.getComparePackage());
        return record;
    }

    /**
     * 针对应用数据进行数据收敛
     *
     * @param recordAppMap 初始化主机流量数据
     * @param appInfoMap   应用信息MAP
     * @return
     */
    private Map<AppTopData, List<AppTopData>> handleAppMap(Map<Integer, List<AppTopData>> recordAppMap, Map<Integer, ApplicationEntity> appInfoMap) {
        //返回结果
        Map<AppTopData, List<AppTopData>> resultMap = new HashMap<>();
        //收敛后的应用数据
        AppTopData appTopData;
        //应用信息
        ApplicationEntity appInfo;
        //收敛后的端口数据
        Map<String, AppTopData> portDataMap;
        //端口的流量数据
        AppTopData portDataInfo;
        //端口key
        String portDataKey;
        //用于检索
        String ipPortIndexKey;
        Set<String> ipPortIndexSet;
        //待收敛的数据
        List<AppTopData> appTopDataList;
        for (Integer appId : recordAppMap.keySet()) {
            //用于存储IP地址和端口号及协议数据（用于在clickhouse查询）
            List<AppTopData> ipPortDataList = new ArrayList<>();
            portDataMap = new HashMap<>();
            ipPortIndexSet = new HashSet<>();
            appTopData = new AppTopData();
            appTopDataList = recordAppMap.get(appId);
            //如果没数据，说明不需要放入应用流量分析里
            if (CollectionUtils.isEmpty(appTopDataList)){
                continue;
            }
            appInfo = appInfoMap.get(appId);
            //进行数据收敛
            if (NetflowStatService.UNKNOWN_APP_ID.equals(appId)) {
                appTopData.setAppName(NetflowStatService.UNKNOWN_APP_NAME);
            } else {
                appTopData.setAppName(appInfo.getApplicationName());
            }
            appTopData.setAppId(appId);
            for (AppTopData data : appTopDataList) {
                ipPortIndexKey = getIndexKey(data);
                if (!ipPortIndexSet.contains(ipPortIndexKey)) {
                    ipPortDataList.add(data);
                    ipPortIndexSet.add(ipPortIndexKey);
                }
                //保存IP+port+协议信息
                appTopData.setInData(appTopData.getInData() + data.getInData());
                appTopData.setOutData(appTopData.getOutData() + data.getOutData());
                appTopData.setSumData(appTopData.getSumData() + data.getCompareData());
                appTopData.setCompareData(appTopData.getCompareData() + data.getCompareData());
                for (AppTopData port : data.getChildList()) {
                    portDataKey = appId + FlowConstant.PLUS + port.getAppId();
                    if (portDataMap.containsKey(portDataKey)) {
                        portDataInfo = portDataMap.get(portDataKey);
                    } else {
                        portDataInfo = new AppTopData();
                        portDataInfo.setAppName(port.getAppName());
                        portDataMap.put(portDataKey, portDataInfo);
                    }
                    portDataInfo.setInData(portDataInfo.getInData() + port.getInData());
                    portDataInfo.setOutData(portDataInfo.getOutData() + port.getOutData());
                    portDataInfo.setSumData(portDataInfo.getSumData() + port.getCompareData());
                    portDataInfo.setCompareData(portDataInfo.getCompareData() + data.getCompareData());
                }
            }
            List<AppTopData> list = new ArrayList<>(portDataMap.values());
            appTopData.setChildList(list);
            resultMap.put(appTopData, ipPortDataList);
        }
        return resultMap;
    }

    /**
     * 获取KEY
     *
     * @param data
     * @return
     */
    private String getIndexKey(NetFlowTopData data) {
        return data.getSourceIp() + ":" + data.getSourcePort() + "to" + data.getDstIp() +
                ":" + data.getDstPort();
    }

    /**
     * 针对流量数据进行排序+简化单位
     *
     * @param list 流量数据
     * @return
     */
    private List<AppTopData> sortAndChangeAppList(List<AppTopData> list) {
        sortTopList(list);
        for (AppTopData appTopData : list) {
            if (CollectionUtils.isNotEmpty(appTopData.getChildList())) {
                sortTopList(appTopData.getChildList());
            }
        }
        //计算百分比
        calcAppNetFlowPercent(list);
        //将数据进行计算
        calcNetFlowUnit(list);
        for (AppTopData appTopData : list) {
            if (CollectionUtils.isNotEmpty(appTopData.getChildList())) {
                calcNetFlowUnit(appTopData.getChildList());
            }
        }
        return list;
    }

    /**
     * 计算流量数据占比
     *
     * @param statTopList 流量监控数据
     */
    private void calcAppNetFlowPercent(List<AppTopData> statTopList) {
        double allData = 0;
        for (AppTopData data : statTopList) {
            allData += data.getCompareData();
        }
        for (AppTopData data : statTopList) {
            data.setNetFlowPercent(MathUtils.getPercentValue(data.getCompareData(), allData));
            data.setNetFlowPercentString(MathUtils.getPercent(data.getCompareData(), allData));
            if (CollectionUtils.isNotEmpty(data.getChildList())) {
                for (AppTopData childData : data.getChildList()) {
                    childData.setNetFlowPercent(MathUtils.getPercentValue(childData.getCompareData(), allData));
                    childData.setNetFlowPercentString(MathUtils.getPercent(childData.getCompareData(), allData));
                }
            }
        }
    }

    /**
     * 更新IP成资产名称
     *
     * @param statList      统计数据
     * @param assetsNameMap 资产名称对应IP关系
     */
    private void updateAssetsName(List<NetFlowTopData> statList, Map<String, String> assetsNameMap) {
        for (NetFlowTopData data : statList) {
            data.setSourceIp(getAssetsName(data.getSourceIp(), assetsNameMap));
            data.setDstIp(getAssetsName(data.getDstIp(), assetsNameMap));
        }
    }

    private String buildDateLimitSql(Date startDateTime, Date endDateTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endDateTime);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        return String.format("( createDateTime >= '%s' and createDateTime < '%s' )", DateUtil.format(startDateTime, FlowConstant.DAY_FORMAT_SIMPLE),
                DateUtil.format(calendar.getTime(), FlowConstant.DAY_FORMAT_SIMPLE));
    }
}
