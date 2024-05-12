package cn.mw.monitor.netflow.service.impl;

import cn.mw.monitor.netflow.annotation.ClickHouseColumn;
import cn.mw.monitor.netflow.constant.FlowConstant;
import cn.mw.monitor.netflow.dao.MWNetflowDao;
import cn.mw.monitor.netflow.entity.CKNetflowCapEntity;
import cn.mw.monitor.netflow.entity.CapDetailChart;
import cn.mw.monitor.netflow.entity.NetFlowDetailChart;
import cn.mw.monitor.netflow.entity.NetflowDetailCacheInfo;
import cn.mw.monitor.netflow.enums.*;
import cn.mw.monitor.netflow.exception.DateNotSelectedException;
import cn.mw.monitor.netflow.param.KibanaPageParam;
import cn.mw.monitor.netflow.param.NetFlowDetailParam;
import cn.mw.monitor.netflow.param.TimeParam;
import cn.mw.monitor.netflow.service.MwNetflowDetailService;
import cn.mw.monitor.netflow.service.clickhouse.ClickhouseUtil;
import cn.mw.monitor.netflow.util.DateUtil;
import cn.mw.monitor.user.dto.GlobalUserInfo;
import cn.mw.monitor.user.service.MWUserService;
import cn.mw.monitor.util.MWUtils;
import cn.mwpaas.common.constant.DateConstant;
import cn.mwpaas.common.enums.DateUnitEnum;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.DateUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author guiquanwnag
 * @datetime 2023/7/19
 * @Description 流量明细服务实现类（用于clickhouse）
 */
@Service
@Slf4j
public class MwNetflowDetailServiceImpl implements MwNetflowDetailService {

    @Autowired
    private ClickhouseUtil clickhouseUtil;

    @Autowired
    private MWUserService mwUserService;

    @Resource
    private MWNetflowDao mwNetflowDao;

    /**
     * 获取流量明细
     *
     * @param param
     * @return
     */
    @Override
    public Reply getNetFlowDetail(NetFlowDetailParam param) {
        //处理时间
        PageInfo pageInfo;
        try {
            updateDetailDate(param);
            //获取表名
            String[] tableNames = getTableName(param.getStartDateTime(), param.getEndDateTime());
            String execSql = generateExecSql(tableNames, param);
            log.info("sql is " + execSql);
            //获取表格数据
            List<CKNetflowCapEntity> list = clickhouseUtil.selectAllData(execSql, CKNetflowCapEntity.class);
            //更新创建时间
            updateCreateTime(list);
            //获取查询到的总数
            String execCountSql = generateExecCountSql(tableNames, param);
            log.info("sql is " + execCountSql);
            long totalCount = clickhouseUtil.countData(execCountSql);
            pageInfo = new PageInfo(list);
            pageInfo.setPageSize(param.getPageSize());
            pageInfo.setPageNum(param.getPageNumber());
            pageInfo.setTotal(totalCount);
            return Reply.ok(pageInfo);
        } catch (DateNotSelectedException e) {
            return Reply.fail(e.getMessage());
        } catch (Exception e) {
            log.error("获取流量明细列表数据失败", e);
            return Reply.fail("获取流量明细列表数据失败");
        }
    }

    private void updateCreateTime(List<CKNetflowCapEntity> list) {
        for (CKNetflowCapEntity cap : list) {
            cap.setCreateTimeString(DateUtil.format(new Date(cap.getCreateTime()),DateConstant.NORM_DATETIME));
        }
    }


    /**
     * 获取流量明细图表
     *
     * @param param
     * @return
     */
    @Override
    public Reply getNetFlowDetailChart(NetFlowDetailParam param) {
        try {
            //先保存数据
            saveCacheInfo(param);
            //处理时间
            updateDetailDate(param);
            //获取表格数据
            List<NetFlowDetailChart> chartList = getNetFlowChart(param);
            return Reply.ok(chartList);
        } catch (DateNotSelectedException e) {
            return Reply.fail(e.getMessage());
        } catch (Exception e) {
            log.error("获取流量明细图表数据失败", e);
            return Reply.fail("获取流量明细图表数据失败");
        }
    }

    /**
     * 获取clickhouse中索引的字段
     *
     * @return
     */
    @Override
    public Reply getNetFlowColumns() {
        try {
            List<Map<String, Object>> mapList = getOperateMap();
            return Reply.ok(mapList);
        } catch (Exception e) {
            log.error("获取clickhouse中索引的字段fail", e);
        }
        return Reply.fail("获取clickhouse中索引的字段失败");
    }

    private String generateExecCountSql(String[] tableNames, NetFlowDetailParam param) {
        String dateSql = buildDateSql(param.getStartDateTime(), param.getEndDateTime());
        String dateLimitSql = buildDateLimitSql(param.getStartDateTime(), param.getEndDateTime());
        String paramLimitSql = buildParamLimitSql(param);
        //表名
        String ckTableName;
        StringBuffer execSql = new StringBuffer();
        for (int i = 0; i < tableNames.length; i++) {
            ckTableName = FlowConstant.CAP_DATABASE + FlowConstant.DATABASE_LINK + tableNames[i];
            execSql.append(String.format(FlowConstant.CAP_DATA_COUNT_SEARCH, ckTableName,
                    concatSql(FlowConstant.AND, dateSql, dateLimitSql, paramLimitSql)));
            if (i != tableNames.length - 1) {
                execSql.append(" union all ");
            }
        }
        return String.format(FlowConstant.CAP_DATA_COUNT_SEARCH_ALL, execSql.toString());
//        String ckTableName = FlowConstant.CAP_DATABASE + FlowConstant.DATABASE_LINK + tableName;
//
//        return String.format(FlowConstant.CAP_DATA_COUNT_SEARCH, ckTableName,
//                concatSql(FlowConstant.AND, dateSql, dateLimitSql, paramLimitSql));
    }

    private String generateExecSql(String[] tableNames, NetFlowDetailParam param) {
        //获取条件判断
        Map<String, String> columnMap = transCKMap(CKNetflowCapEntity.class);
        StringBuffer stringBuffer = new StringBuffer();
        for (String key : columnMap.keySet()) {
            stringBuffer.append(key).append(" as ").append(columnMap.get(key)).append(",");
        }
        stringBuffer.setLength(stringBuffer.length() - 1);
        String dateSql = buildDateSql(param.getStartDateTime(), param.getEndDateTime());
        String dateLimitSql = buildDateLimitSql(param.getStartDateTime(), param.getEndDateTime());
        String paramLimitSql = buildParamLimitSql(param);
        String orderByName = StringUtils.isEmpty(param.getSortColumn()) ? "createTime" : columnMap.get(param.getSortColumn());
        String sortTyp = StringUtils.isEmpty(param.getSortType()) ? "DESC" : param.getSortType();
        //表名
        String ckTableName;
        StringBuffer execSql = new StringBuffer();
        for (int i = 0; i < tableNames.length; i++) {
            ckTableName = FlowConstant.CAP_DATABASE + FlowConstant.DATABASE_LINK + tableNames[i];
            execSql.append(String.format(FlowConstant.CAP_DATA_SEARCH, stringBuffer.toString(), ckTableName,
                    concatSql(FlowConstant.AND, dateSql, dateLimitSql, paramLimitSql)));
            if (i != tableNames.length - 1) {
                execSql.append(" union all ");
            }
        }
        //统计计算排序
        return String.format(FlowConstant.CAP_DATA_SEARCH_PAGE, execSql.toString(), orderByName,
                sortTyp, param.getPageNumber(), param.getPageSize());
//        for (String name : tableNames) {
//
//        }
//        //表名
//        String ckTableName = FlowConstant.CAP_DATABASE + FlowConstant.DATABASE_LINK + tableName;
//
//
//        return String.format(FlowConstant.CAP_DATA_SEARCH, stringBuffer.toString(), ckTableName,
//                concatSql(FlowConstant.AND, dateSql, dateLimitSql, paramLimitSql), orderByName,
//                sortTyp, param.getPageNumber(), param.getPageSize());
    }

    private String buildDateLimitSql(Date startDateTime, Date endDateTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endDateTime);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        return String.format("( createDateTime >= '%s' and createDateTime < '%s' )", DateUtil.format(startDateTime, FlowConstant.DAY_FORMAT_SIMPLE),
                DateUtil.format(calendar.getTime(), FlowConstant.DAY_FORMAT_SIMPLE));
    }

    /**
     * 构建条件查询SQL，由于kibana只支持单层逻辑，所以直接串联起来
     *
     * @param param
     * @return
     */
    private String buildParamLimitSql(NetFlowDetailParam param) {
        StringBuffer stringBuffer = new StringBuffer();
        if (CollectionUtils.isNotEmpty(param.getKibanaList())) {
            //类型
            KibanaType kibanaType;
            //关键词
            String key = null;
            //操作符
            OperateType operateType = null;
            //对应值
            String value = null;
            //多个条件之间的
            RelationType relationType;
            for (KibanaPageParam page : param.getKibanaList()) {
                kibanaType = KibanaType.getKibanaType(page.getType());
                switch (kibanaType) {
                    case KEY:
                        key = page.getValue();
                        break;
                    case OPERATE:
                        operateType = OperateType.getOperateType(page.getValue());
                        break;
                    case VALUE:
                        value = page.getValue();
                        break;
                    case RELATION:
                        relationType = RelationType.valueOf(page.getValue());
                        stringBuffer.append(geneKibanaQueryBuild(key, operateType, value));
                        switch (relationType) {
                            case and:
                                stringBuffer.append(FlowConstant.AND).append(FlowConstant.SPACE);
                                break;
                            case or:
                                stringBuffer.append(FlowConstant.OR).append(FlowConstant.SPACE);
                                break;
                        }
                        //clear
                        key = null;
                        value = null;
                        operateType = null;
                        break;
                    default:
                        break;
                }
            }
            stringBuffer.append(geneKibanaQueryBuild(key, operateType, value));
        }
        return stringBuffer.toString();
    }

    /**
     * 构建KIBANA查询条件
     *
     * @param key         查询ES的column
     * @param operateType 条件符
     * @param value       值
     * @return
     */
    private String geneKibanaQueryBuild(String key, OperateType operateType, String value) {
        if (StringUtils.isBlank(key) || StringUtils.isEmpty(value) || operateType == null) {
            return FlowConstant.EMPTY_STRING;
        }
        switch (operateType) {
            case EQUAL:
                return key + FlowConstant.EQUAL + value;
            case EQUAL_ALL:
                break;
            case LESS_THAN:
            case LESS_THAN_EQUAL_TO:
            case GREATER_THAN:
            case GREATER_THAN_EQUAL_TO:
                return key + operateType.getOperateValue() + value;
            default:
                break;
        }
        return FlowConstant.EMPTY_STRING;
    }

    private String buildDateSql(Date startDateTime, Date endDateTime) {
        return String.format("( createTime > %d and createTime < %d )", startDateTime.getTime(), endDateTime.getTime());
    }

    /**
     * 获取类中属性与clickhouse属性的映射关系
     *
     * @param clazz
     * @return
     */
    private Map<String, String> transCKMap(Class<?> clazz) {
        Map<String, String> columnMap = new HashMap();
        Field[] fields = clazz.getDeclaredFields();
        ClickHouseColumn column;
        for (Field field : fields) {
            //没有注解不返回
            if (!field.isAnnotationPresent(ClickHouseColumn.class)) {
                continue;
            }
            column = field.getAnnotation(ClickHouseColumn.class);
            columnMap.put(field.getName(), column.name());
        }
        return columnMap;
    }


    /**
     * 获取操作MAP
     *
     * @return
     */
    private List<Map<String, Object>> getOperateMap() {
        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, Object> opMap;
        Class netFlowCap = CKNetflowCapEntity.class;
        Field[] fields = netFlowCap.getDeclaredFields();
        IconType iconType;
        for (Field field : fields) {
            //没有注解不返回
            if (!field.isAnnotationPresent(ClickHouseColumn.class)) {
                continue;
            }
            opMap = new HashMap<>();
            List<OperateType> operateList = OperateType.getOperateValueList(FieldType.getField(field.getType()));
            List<String> opList = listToString(operateList);
            opMap.put(field.getName(), opList);
            iconType = IconType.getIconType(field.getType());
            opMap.put("icon", iconType.getColumnCode());
            resultList.add(opMap);
        }
        return resultList;
    }

    /**
     * 将操作列表转换成字符串列表
     *
     * @param operateList
     * @return
     */
    private List<String> listToString(List<OperateType> operateList) {
        List<String> list = new ArrayList<>();
        if (CollectionUtils.isEmpty(operateList)) {
            return list;
        }
        for (OperateType type : operateList) {
            list.add(type.getOperateValue());
        }
        return list;
    }

    /**
     * 更新时间字段
     *
     * @param param
     */
    private void updateDetailDate(NetFlowDetailParam param) {
        if (param.dateParamIsNotEmpty()) {
            param.setStartDateTime(getDateTime(param.getStartTime()));
            param.setEndDateTime(getDateTime(param.getEndTime()));
        } else {
            throw new DateNotSelectedException("请选择时间段");
        }
    }

    /**
     * 获取时间数据
     *
     * @param timeParam
     * @return
     */
    private Date getDateTime(TimeParam timeParam) {
        Date dateTime;
        switch (timeParam.getType()) {
            case TimeParam.DATE_TYPE_ABSOLUTE:
                dateTime = MWUtils.strToDateLong(timeParam.getValue());
                break;
            case TimeParam.DATE_TYPE_NOW:
                dateTime = new Date();
                break;
            case TimeParam.DATE_TYPE_RELATIVE:
                dateTime = addTime(timeParam);
                break;
            default:
                dateTime = new Date();
                break;
        }
        return dateTime;
    }

    /**
     * relative时，处理时间数据
     *
     * @param timeParam
     * @return
     */
    private Date addTime(TimeParam timeParam) {
        Date returnDate;
        Date nowDate = new Date();
        TimeType timeType = TimeType.getTimeType(timeParam.getUnit());
        int amount = -Integer.parseInt(timeParam.getValue());
        switch (timeType) {
            case SECOND:
                returnDate = DateUtils.addSeconds(nowDate, amount);
                break;
            case MINUTE:
                returnDate = DateUtils.addMinutes(nowDate, amount);
                break;
            case HOUR:
                returnDate = DateUtils.addHours(nowDate, amount);
                break;
            case DAY:
                returnDate = DateUtils.addDays(nowDate, amount);
                break;
            case WEEK:
                returnDate = DateUtils.addWeeks(nowDate, amount);
                break;
            case MONTH:
                returnDate = DateUtils.addMonths(nowDate, amount);
                break;
            case YEAR:
                returnDate = DateUtils.addYears(nowDate, amount);
                break;
            default:
                returnDate = nowDate;
                break;
        }
        if (timeParam.isRound()) {
            return roundDate(timeType, nowDate);
        }
        return returnDate;
    }

    /**
     * 获取抹零后的时间
     *
     * @param timeType 时间类别
     * @param nowDate  待修改的时间
     * @return
     */
    private Date roundDate(TimeType timeType, Date nowDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(nowDate);
        switch (timeType) {
            case SECOND:
                break;
            case MINUTE:
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.set(Calendar.SECOND, 0);
                break;
            case HOUR:
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MINUTE, 0);
                break;
            case DAY:
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                break;
            case WEEK:
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                break;
            case MONTH:
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                break;
            case YEAR:
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.DAY_OF_YEAR, 1);
                break;
            default:
                break;
        }
        return calendar.getTime();
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
//        long betweenDays = DateUtil.betweenDay(startTime, endTime, true);
//        long betweenMonths = DateUtil.betweenMonth(startTime, endTime, true);
//        long betweenYears = DateUtil.betweenYear(startTime, endTime, true);
//        if (betweenYears > 0) {
//            return FlowConstant.CIICKHOUSE_INDEX_PREFIX + FlowConstant.TABLE_SEP + FlowConstant.ALL_PREFIX;
//        }
//        if (betweenMonths > 0) {
//            return FlowConstant.CIICKHOUSE_INDEX_PREFIX + FlowConstant.TABLE_SEP + DateUtil.format(startTime, FlowConstant.YEAR_FORMAT)
//                    + FlowConstant.ALL_PREFIX;
//        }
//        if (betweenDays > 0) {
//            return FlowConstant.CIICKHOUSE_INDEX_PREFIX + FlowConstant.TABLE_SEP + DateUtil.format(startTime, FlowConstant.MONTH_FORMAT)
//                    + FlowConstant.ALL_PREFIX;
//        }
//        return FlowConstant.CIICKHOUSE_INDEX_PREFIX + FlowConstant.TABLE_SEP + DateUtil.format(startTime, FlowConstant.DAY_FORMAT);
        return tableArray;
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
     * 保存流量明细缓存数据
     *
     * @param param
     */
    private void saveCacheInfo(NetFlowDetailParam param) {
        try {
            GlobalUserInfo userInfo = mwUserService.getGlobalUser();
            NetflowDetailCacheInfo cacheInfo;
            String oldCache = mwNetflowDao.getNetflowCacheInfo(userInfo.getUserId());
            if (StringUtils.isNotEmpty(oldCache)) {
                cacheInfo = JSON.parseObject(oldCache, NetflowDetailCacheInfo.class);
            } else {
                cacheInfo = new NetflowDetailCacheInfo();
                cacheInfo.setSelectedColumns(new ArrayList<>());
            }
            if (CollectionUtils.isNotEmpty(param.getKibanaList())) {
                StringBuffer stringBuffer = new StringBuffer();
                for (KibanaPageParam page : param.getKibanaList()) {
                    stringBuffer.append(page.getValue());
                }
                cacheInfo.setKibanaInfo(stringBuffer.toString());
            } else {
                cacheInfo.setKibanaInfo("");
            }
            cacheInfo.setStartTime(param.getStartTime());
            cacheInfo.setEndTime(param.getEndTime());
            //先删除
            mwNetflowDao.deleteNetflowCacheInfo(userInfo.getUserId());
            //再增加
            mwNetflowDao.saveNetlowCacheInfo(userInfo.getUserId(), JSON.toJSONString(cacheInfo));
        } catch (Exception e) {
            log.error("保存流量明细缓存数据失败", e);
        }
    }

    /**
     * 获取流量详情分段数据信息
     *
     * @param param
     * @return
     */
    private List<NetFlowDetailChart> getNetFlowChart(NetFlowDetailParam param) {
        List<NetFlowDetailChart> chartList;
        try {
            String[] tableName = getTableName(param.getStartDateTime(), param.getEndDateTime());
            String execSql = generateChartSql(tableName, param);
            log.info("sql is " + execSql);
            //获取表格数据
            List<CapDetailChart> list = clickhouseUtil.selectAll(execSql, CapDetailChart.class);
            chartList = handleCapList(list, param.getStartDateTime(), param.getEndDateTime());
            return chartList;
        } catch (Exception e) {
            log.error("获取流量详情分段数据信息失败", e);
        }
        return new ArrayList<>();
    }

    private List<NetFlowDetailChart> handleCapList(List<CapDetailChart> list, Date startTime, Date endTime) {
        List<CapDetailChart> emptyChartList = new ArrayList<>();
        Set<Integer> intervalSet = new HashSet<>();
        long intervalMillis = getIntervalMillis(startTime, endTime);
        //判断从开始时间到结束时间跨越多少时间间隔
        long maxTimeInterval = (endTime.getTime() - startTime.getTime()) / intervalMillis;
        for (CapDetailChart result : list) {
            //插入折线图数据
            intervalSet.add(result.getTimeInterval());
        }
        //根据resultIntervalMap补全空数据的时间间隔
        CapDetailChart emptyDetail;
        for (int i = 0; i < maxTimeInterval; i++) {
            if (!intervalSet.contains(i)) {
                emptyDetail = new CapDetailChart();
                emptyDetail.setTimeInterval(i);
                emptyDetail.setTimes(0);
                emptyChartList.add(emptyDetail);
            }
        }
        list.addAll(emptyChartList);
        //进行数据排序，同时修改数据格式
        return sortAndChangeUnit(list, startTime, endTime);
    }

    private List<NetFlowDetailChart> sortAndChangeUnit(List<CapDetailChart> capList, Date startTime, Date endTime) {
        List<NetFlowDetailChart> chartList = new ArrayList<>();
        long intervalMillis = getIntervalMillis(startTime, endTime);
        //判断从开始时间到结束时间跨越多少时间间隔
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startTime);
        //sort
        List<CapDetailChart> sortedList = capList.stream()
                .sorted(Comparator.comparing(CapDetailChart::getTimeInterval)).collect(Collectors.toList());
        NetFlowDetailChart chart;
        for (CapDetailChart chartDetail : sortedList) {
            chart = new NetFlowDetailChart();
            //时间转换
            calendar.add(Calendar.MILLISECOND, (int) intervalMillis);
            chart.setCount(chartDetail.getTimes());
            chart.setColumnName(DateUtils.format(calendar.getTime(), DateConstant.NORM_DATETIME));
            chartList.add(chart);
        }
        return chartList;

    }

    private String generateChartSql(String[] tableNames, NetFlowDetailParam param) {
        String dateSql = buildDateSql(param.getStartDateTime(), param.getEndDateTime());
        String dateLimitSql = buildDateLimitSql(param.getStartDateTime(), param.getEndDateTime());
        String paramLimitSql = buildParamLimitSql(param);
        //表名
        String ckTableName;
        StringBuffer execSql = new StringBuffer();
        for (int i = 0; i < tableNames.length; i++) {
            ckTableName = FlowConstant.CAP_DATABASE + FlowConstant.DATABASE_LINK + tableNames[i];
            execSql.append(String.format(FlowConstant.CAP_CHART_NETFLOW, param.getStartDateTime().getTime(),
                    getIntervalMillis(param.getStartDateTime(), param.getEndDateTime()),
                    ckTableName, concatSql(FlowConstant.AND, dateSql, dateLimitSql, paramLimitSql)));
            if (i != tableNames.length - 1) {
                execSql.append(" union all ");
            }
        }
        //表名
//        String ckTableName = FlowConstant.CAP_DATABASE + FlowConstant.DATABASE_LINK + tableName;

        return execSql.toString();
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
}
