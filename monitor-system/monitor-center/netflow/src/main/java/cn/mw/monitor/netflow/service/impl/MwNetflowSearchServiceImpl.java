package cn.mw.monitor.netflow.service.impl;

import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.netflow.constant.FlowConstant;
import cn.mw.monitor.netflow.dao.NetflowTreeManageDao;
import cn.mw.monitor.netflow.service.MwNetflowDetailService;
import cn.mw.monitor.netflow.service.clickhouse.ClickhouseUtil;
import cn.mw.monitor.netflow.util.DateUtil;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.assets.param.QueryAssetsInterfaceParam;
import cn.mw.monitor.service.assets.param.QueryTangAssetsParam;
import cn.mw.monitor.service.netflow.api.MwNetflowCommonService;
import cn.mw.monitor.service.netflow.entity.NetflowResult;
import cn.mw.monitor.service.netflow.param.NetflowSearchParam;
import cn.mw.monitor.user.dto.GlobalUserInfo;
import cn.mw.monitor.user.service.MWUserService;
import cn.mw.monitor.util.NewUnits;
import cn.mw.monitor.util.UnitsUtil;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.Stats;
import org.elasticsearch.search.aggregations.metrics.StatsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author guiquanwnag
 * @datetime 2023/8/28
 * @Description 流量检索服务类
 */
@Service
@Slf4j
public class MwNetflowSearchServiceImpl implements MwNetflowCommonService {

    /**
     * 存储类别(1:ES  2:clickhouse)
     */
    @Value("${cap.storage.type}")
    private int storageType;

    private static final String RULE_PROCESSOR = "captcp";

    private final static String ES_NETFLOW_ALL_INDEX = RULE_PROCESSOR + "*";

    private final static int ES_MAX_SIZE = 10000;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private ClickhouseUtil clickhouseUtil;

    @Autowired
    private MwAssetsManager mwAssetsManager;

    @Autowired
    private MWUserService mwUserService;


    /**
     * 根据查询参数，获取流量数据
     *
     * @param searchParam 流量查询数据
     * @return
     */
    @Override
    public List<NetflowResult> getNetflowResult(NetflowSearchParam searchParam) {
        List<NetflowResult> resultList = new ArrayList<>();
        NetflowResult result;
        GlobalUserInfo globalUser = mwUserService.getGlobalUser();
        QueryTangAssetsParam param = new QueryTangAssetsParam();
        param.setAssetsIds(searchParam.getAssetsId());
        param.setIsQueryAssetsState(false);
        param.setSkipDataPermission(false);
        param.setUserId(globalUser.getUserId());
        List<MwTangibleassetsTable> assetList = mwAssetsManager.getAssetsTable(param);
        if (CollectionUtils.isEmpty(assetList)) {
            for (String id : searchParam.getAssetsId()) {
                resultList.add(generateEmptyResult(id));
            }
        } else {
            for (MwTangibleassetsTable asset : assetList) {
                if (StringUtils.isEmpty(asset.getInBandIp())) {
                    result = generateEmptyResult(asset.getId());
                    resultList.add(result);
                } else {
                    //根据条件计算出流量数据
                    result = getNetFlowList(asset.getInBandIp(), searchParam.getStartTime(), searchParam.getEndTime());
                    result.setAssetsId(asset.getId());
                    resultList.add(result);
                }
            }
        }
        return resultList;
    }

    /**
     * @param inBandIp
     * @param startTime
     * @param endTime
     * @return
     */
    private NetflowResult getNetFlowList(String inBandIp, Date startTime, Date endTime) {
        NetflowResult result = new NetflowResult();
        //计算入流量
        NetflowResult inResult = getMaxAndSum(inBandIp, startTime, endTime, true);
        result.setInSum(inResult.getInSum());
        result.setInMax(inResult.getInMax());
        //计算出流量
        NetflowResult outResult = getMaxAndSum(inBandIp, startTime, endTime, false);
        result.setOutSum(outResult.getOutSum());
        result.setOutMax(outResult.getOutMax());
        return result;
    }

    private NetflowResult getMaxAndSum(String ip, Date startTime, Date endTime, boolean isIn) {
        NetflowResult result = new NetflowResult();
        if (MwNetflowDetailService.STORAGE_CLICKHOUSE == storageType) {
            //获取表名
            String[] tableNames = getTableName(startTime, endTime);
            //获取查询到的总数
            String execCountSql = generateSumSql(tableNames, ip, startTime, endTime, isIn);
            log.info("sum sql is " + execCountSql);
            long sum = clickhouseUtil.countData(execCountSql);

            execCountSql = generateMaxSql(tableNames, ip, startTime, endTime, isIn);
            log.info("max sql is " + execCountSql);
            long max = clickhouseUtil.countData(execCountSql);

            //计算单位
            String maxValue = UnitsUtil.getValueWithUnits(String.valueOf(max), NewUnits.B.getUnits());
            String sumValue = UnitsUtil.getValueWithUnits(String.valueOf(sum), NewUnits.B.getUnits());
            if (isIn) {
                result.setInSum(sumValue);
                result.setInMax(maxValue);
            } else {
                result.setOutSum(sumValue);
                result.setOutMax(maxValue);
            }
        } else {
            try {
                //计算入向流量
                SearchRequest searchRequest = new SearchRequest();
                searchRequest.indices(getEsIndex(startTime, endTime));
                SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
                boolQueryBuilder.must(QueryBuilders.rangeQuery("createTime")
                        .gte(startTime.getTime())
                        .lte(endTime.getTime()));
                //根据资产ID获取对应的IP信息
                BoolQueryBuilder ipShouldQueryBuilder = new BoolQueryBuilder();
                if (isIn) {
                    ipShouldQueryBuilder.should(QueryBuilders.termQuery("srcIp", ip));
                } else {
                    ipShouldQueryBuilder.should(QueryBuilders.termQuery("destIp", ip));
                }
                boolQueryBuilder.must(ipShouldQueryBuilder);

                // 添加统计值聚合
                StatsAggregationBuilder statsAggregation = AggregationBuilders.stats("stats").field("length");
                searchSourceBuilder.aggregation(statsAggregation);

                searchSourceBuilder.query(boolQueryBuilder);

                searchSourceBuilder.size(ES_MAX_SIZE);
                searchRequest.source(searchSourceBuilder);
                SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

                // 处理搜索结果
                // 从searchResponse中提取最大值和统计值聚合结果
                Stats statsAggResult = searchResponse.getAggregations().get("stats");

                double sum = statsAggResult.getSum();
                double max = statsAggResult.getMax();
                //计算单位
                String maxValue = UnitsUtil.getValueWithUnits(String.valueOf(max), NewUnits.B.getUnits());
                String sumValue = UnitsUtil.getValueWithUnits(String.valueOf(sum), NewUnits.B.getUnits());
                if (isIn) {
                    result.setInSum(sumValue);
                    result.setInMax(maxValue);
                } else {
                    result.setOutSum(sumValue);
                    result.setOutMax(maxValue);
                }
            } catch (Exception e) {
                log.error("流量统计-->获取ES数据失败", e);
                result = generateEmptyResult(null);
            }
        }
        return result;
    }

    private String generateMaxSql(String[] tableNames, String ip, Date startTime, Date endTime, boolean isIn) {
        String dateSql = buildDateSql(startTime, endTime);
        String dateLimitSql = buildDateLimitSql(startTime, endTime);
        String paramLimitSql = buildParamLimitSql(ip, isIn);

        //表名
        String ckTableName;
        StringBuffer execSql = new StringBuffer();
        for (int i = 0; i < tableNames.length; i++) {
            ckTableName = FlowConstant.CAP_DATABASE + FlowConstant.DATABASE_LINK + tableNames[i];
            execSql.append(String.format(FlowConstant.CAP_MAX_SEARCH, ckTableName,
                    concatSql(FlowConstant.AND, dateSql, dateLimitSql, paramLimitSql)));
            if (i != tableNames.length - 1) {
                execSql.append(" union all ");
            }
        }
        return String.format(FlowConstant.CAP_MAX_SEARCH_ALL, execSql.toString());
    }

    private String generateSumSql(String[] tableNames, String ip, Date startTime, Date endTime, boolean isIn) {
        String dateSql = buildDateSql(startTime, endTime);
        String dateLimitSql = buildDateLimitSql(startTime, endTime);
        String paramLimitSql = buildParamLimitSql(ip, isIn);

        //表名
        String ckTableName;
        StringBuffer execSql = new StringBuffer();
        for (int i = 0; i < tableNames.length; i++) {
            ckTableName = FlowConstant.CAP_DATABASE + FlowConstant.DATABASE_LINK + tableNames[i];
            execSql.append(String.format(FlowConstant.CAP_SUM_SEARCH, ckTableName,
                    concatSql(FlowConstant.AND, dateSql, dateLimitSql, paramLimitSql)));
            if (i != tableNames.length - 1) {
                execSql.append(" union all ");
            }
        }
        return String.format(FlowConstant.CAP_SUM_SEARCH_ALL, execSql.toString());
    }

    private String buildParamLimitSql(String ip, boolean isIn) {
        if (isIn) {
            return String.format("( srcIp = '%s' )", ip);
        } else {
            return String.format("( destIp = '%s' )", ip);
        }
    }

    private String buildDateSql(Date startDateTime, Date endDateTime) {
        return String.format("( createTime > %d and createTime < %d )", startDateTime.getTime(), endDateTime.getTime());
    }

    private String buildDateLimitSql(Date startDateTime, Date endDateTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endDateTime);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        return String.format("( createDateTime >= '%s' and createDateTime < '%s' )", DateUtil.format(startDateTime, FlowConstant.DAY_FORMAT_SIMPLE),
                DateUtil.format(calendar.getTime(), FlowConstant.DAY_FORMAT_SIMPLE));
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

    private NetflowResult generateEmptyResult(String assetId) {
        NetflowResult result = new NetflowResult();
        result.setAssetsId(assetId);
        result.setInSum("0B");
        result.setOutSum("0B");
        result.setInMax("0B");
        result.setOutMax("0B");
        return result;
    }


    /**
     * 获取流量监控数据列表（从ES获取）
     *
     * @param interfaceList
     * @param startTime
     * @param endTime
     * @return
     */
    private NetflowResult getNetFlowList(List<QueryAssetsInterfaceParam> interfaceList, Date startTime, Date endTime) {
        NetflowResult result = new NetflowResult();
        //计算入流量
        NetflowResult inResult = getMaxAndSum(interfaceList, startTime, endTime, true);
        result.setInSum(inResult.getInSum());
        result.setInMax(inResult.getInMax());
        //计算出流量
        NetflowResult outResult = getMaxAndSum(interfaceList, startTime, endTime, false);
        result.setOutSum(outResult.getOutSum());
        result.setOutMax(outResult.getOutMax());
        return result;
    }

    private NetflowResult getMaxAndSum(List<QueryAssetsInterfaceParam> interfaceList, Date startTime, Date endTime, boolean isIn) {
        NetflowResult result = new NetflowResult();
        try {
            //计算入向流量
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.indices(getEsIndex(startTime, endTime));
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
            boolQueryBuilder.must(QueryBuilders.rangeQuery("createTime")
                    .gte(startTime.getTime())
                    .lte(endTime.getTime()));
            //根据资产ID获取对应的IP信息
            BoolQueryBuilder ipShouldQueryBuilder = new BoolQueryBuilder();
            Set<String> ipSet = new HashSet<>();
            Set<Integer> indexSet = new HashSet<>();
            for (QueryAssetsInterfaceParam param : interfaceList) {
                if (StringUtils.isNotEmpty(param.getIp())) {
                    ipSet.add(param.getIp());
                }
                if (StringUtils.isNotEmpty(param.getHostIp())) {
                    ipSet.add(param.getHostIp());
                }

                if (param.getIfIndex() != null) {
                    indexSet.add(param.getIfIndex());
                }
            }
            if (CollectionUtils.isNotEmpty(ipSet)) {
                for (String ip : ipSet) {
                    ipShouldQueryBuilder = ipShouldQueryBuilder.should(QueryBuilders.termQuery("sender", ip));
                }
            } else {
                return generateEmptyResult(null);
            }
            boolQueryBuilder.must(ipShouldQueryBuilder);

            //获取所有接口索引
            BoolQueryBuilder shouldQueryBuilder = new BoolQueryBuilder();
            BoolQueryBuilder indexShouldQueryBuilder = new BoolQueryBuilder();
            for (Integer ifIndex : indexSet) {
                if (isIn) {
                    indexShouldQueryBuilder = indexShouldQueryBuilder.should(QueryBuilders.termQuery("inputInterface", ifIndex));
                } else {
                    indexShouldQueryBuilder = indexShouldQueryBuilder.should(QueryBuilders.termQuery("outputInterface", ifIndex));
                }
            }
            shouldQueryBuilder.should(indexShouldQueryBuilder);
            boolQueryBuilder.must(shouldQueryBuilder);

            // 添加统计值聚合
            StatsAggregationBuilder statsAggregation = AggregationBuilders.stats("stats").field("inBytes");
            searchSourceBuilder.aggregation(statsAggregation);

            searchSourceBuilder.query(boolQueryBuilder);

            searchSourceBuilder.size(ES_MAX_SIZE);
            searchRequest.source(searchSourceBuilder);
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

            // 处理搜索结果
            // 从searchResponse中提取最大值和统计值聚合结果
            Stats statsAggResult = searchResponse.getAggregations().get("stats");

            double sum = statsAggResult.getSum();
            double max = statsAggResult.getMax();
            //计算单位
            String maxValue = UnitsUtil.getValueWithUnits(String.valueOf(max), NewUnits.B.getUnits());
            String sumValue = UnitsUtil.getValueWithUnits(String.valueOf(sum), NewUnits.B.getUnits());
            if (isIn) {
                result.setInSum(sumValue);
                result.setInMax(maxValue);
            } else {
                result.setOutSum(sumValue);
                result.setOutMax(maxValue);
            }
        } catch (Exception e) {
            log.error("流量统计-->获取ES数据失败", e);
            result = generateEmptyResult(null);
        }
        return result;
    }

    private String getEsIndex(Date startTime, Date endTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startTime);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String yearMonthDay = dateFormat.format(startTime);
        String startIndex = new StringBuffer(RULE_PROCESSOR).append(yearMonthDay).toString();

        calendar = Calendar.getInstance();
        calendar.setTime(endTime);
        yearMonthDay = dateFormat.format(endTime);
        String endIndex = new StringBuffer(RULE_PROCESSOR).append(yearMonthDay).toString();

        if (startIndex != null && startIndex.equals(endIndex)) {
            return startIndex;
        } else {
            return ES_NETFLOW_ALL_INDEX;
        }
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
}
