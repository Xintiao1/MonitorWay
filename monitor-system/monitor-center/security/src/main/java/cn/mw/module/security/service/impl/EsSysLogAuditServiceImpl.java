package cn.mw.module.security.service.impl;

import cn.mw.module.security.dto.*;
import cn.mw.module.security.service.EsSysLogAuditService;
import cn.mw.monitor.assets.utils.ExportExcel;
import cn.mw.monitor.security.dao.EsSysLogAuditDao;
import cn.mw.monitor.security.dao.EsSysLogRuleDao;
import cn.mw.monitor.service.model.param.QueryModelAssetsParam;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.util.MWUtils;
import cn.mw.monitor.weixinapi.DelFilter;
import cn.mw.monitor.weixinapi.MessageContext;
import cn.mw.monitor.weixinapi.MwRuleSelectParam;
import cn.mwpaas.common.enums.DateUnitEnum;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.DateUtils;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.crypto.Cipher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static cn.mw.module.security.dto.MwDataSourceType.ELASTICSEARCH;
import static cn.mw.monitor.util.MWUtils.getUTCToCST;

/**
 * @author qzg
 * @date 2021/12/22
 */
@Service
@Slf4j
public class EsSysLogAuditServiceImpl implements EsSysLogAuditService {
    @Resource
    private EsSysLogRuleDao esSysLogRuleDao;

    @Resource
    private EsSysLogAuditDao esSysLogAuditDao;

    @Value("${model.assets.enable}")
    private boolean modelAssetEnable;
    @Resource
    private MwModelViewCommonService mwModelViewCommonService;

    @Resource
    private DataSourceConfigureServiceImpl dataSourceImpl;


    private final int pageSize = 1000;

    //每次滚动查询es数据的条数
    static final int scrollSize = 10000;

    private final List<String> listStr = Arrays.asList("facility_label", "severity_label", "host");

    private final List<String> fieldList = Arrays.asList("message", "@timestamp", "facility_label", "host", "severity_label");

    @Override
    public Reply getSystemLogInfos(EsSysLogAuditQueryDTO param) {
        try {
            EsDataSourceListDto dto = new EsDataSourceListDto();
            List<Map<String, Object>> assetsIpInfo = new ArrayList<>();
            if (modelAssetEnable) {//获取资源中心下的资产数据
                assetsIpInfo = mwModelViewCommonService.getModelListInfoByPerm(new QueryModelAssetsParam());

            } else {//获取mw_tangibleassets_table表中的数据
                assetsIpInfo = esSysLogAuditDao.getAllAssetsInfoByIp();
            }
            Map<String, List> assetsIpInfoMap = new HashMap();
            for (Map<String, Object> m : assetsIpInfo) {
                String ip = "";
                String name = "";
                if (m.get("inBandIp") != null) {
                    ip = m.get("inBandIp").toString();
                }
                if (m.get("instanceName") != null) {
                    name = m.get("instanceName").toString();
                }
                if (assetsIpInfoMap.containsKey(ip)) {
                    List<String> listName = assetsIpInfoMap.get(ip);
                    listName.add(name);
                    assetsIpInfoMap.put(ip, listName);
                } else {
                    List<String> listName = new ArrayList<>();
                    listName.add(name);
                    assetsIpInfoMap.put(ip, listName);
                }
            }
            List<Map<String, Object>> listMap = new ArrayList<>();
            Date endTime = new Date();
            Date startTime = DateUtils.addMinutes(endTime, -10);
            if (param.getDateType() != null) {
                switch (param.getDateType()) {//1:10分钟、2:1小时、3:12小时、4:1天、5:一周 6:一个月 7：自定义
                    case 1:
                        startTime = DateUtils.addMinutes(endTime, -10);
                        break;
                    case 2:
                        startTime = DateUtils.addHours(endTime, -1);
                        break;
                    case 3:
                        startTime = DateUtils.addHours(endTime, -12);
                        break;
                    case 4:
                        startTime = DateUtils.addDays(endTime, -1);
                        break;
                    case 5:
                        startTime = DateUtils.addDays(endTime, -7);
                        break;
                    case 6:
                        startTime = DateUtils.addMonths(endTime, -1);
                        break;
                    case 7:
                        if (param.getStartTime() == null || param.getEndTime() == null) {
                            return Reply.fail("自定义时间不可为空");
                        }
                        startTime = param.getStartTime();
                        endTime = param.getEndTime();
                        break;
                    default:
                        break;
                }
            }
            long count = 0l;
            if (dto != null && dto.getInfoList() != null && dto.getInfoList().size() > 0) {
                for (EsDataSourceListInfoDto p : dto.getInfoList()) {
                    //暂时日志来源于es
                    if (ELASTICSEARCH.getType().equals(p.getDataSourceType())) {
                        RestHighLevelClient client = p.getClient();
                        String queryEsIndex = p.getQueryEsIndex();
                        //数据源名称
                        String dataSourceName = p.getDataSourceName();

                        //条件组合查询
                        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
                        queryBuilder.must(QueryBuilders.rangeQuery("@timestamp").from(MWUtils.getUtcTime(DateUtils.formatDateTime(startTime))).to(MWUtils.getUtcTime(DateUtils.formatDateTime(endTime))));
                        //全字段模糊查询
                        if (param.getQueryFieldList() != null && param.getQueryFieldList().size() > 0) {
                            BoolQueryBuilder queryBuilder2 = QueryBuilders.boolQuery();
                            for (EsQueryParam esQueryParam : param.getQueryFieldList()) {
                                //字符串
                                if ((!Strings.isNullOrEmpty(esQueryParam.getQueryField())) && (!"dataSourceName".equals(esQueryParam.getQueryField())) && (!Strings.isNullOrEmpty(esQueryParam.getQueryValue()))) {
                                    queryBuilder2 = queryBuilder2.should(QueryBuilders.wildcardQuery(esQueryParam.getQueryField() + ".keyword", "*" + (esQueryParam.getQueryValue().trim().replaceFirst(" ", "*")) + "*"));
                                }
                            }
                            queryBuilder.must(queryBuilder2);
                        }
                        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                        searchSourceBuilder.from((param.getPageNumber() - 1) * param.getPageSize());
                        searchSourceBuilder.size(param.getPageSize());
                        //返回指定字段数据
                        String[] includes = fieldList.toArray(new String[fieldList.size()]);
                        FetchSourceContext sourceContext = new FetchSourceContext(true, includes, null);
                        searchSourceBuilder.fetchSource(sourceContext);
                        List<String> sortField = Arrays.asList("message", "facility_label", "severity_label");
                        //排序
                        //字符串的排序需要使用不分词类型
                        if (!Strings.isNullOrEmpty(param.getSortField())) {
                            //时间排序
                            if ("@timestamp".equals(param.getSortField())) {
                                searchSourceBuilder.sort("@timestamp", (param.getSortType() == 1 ? SortOrder.DESC : SortOrder.ASC));
                            } else if (sortField.contains(param.getSortField())) {//字符串排序
                                searchSourceBuilder.sort(param.getSortField() + (".keyword"), (param.getSortType() == 1 ? SortOrder.DESC : SortOrder.ASC));
                            }
                        }
                        //设置超时时间
                        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
                        searchSourceBuilder.query(queryBuilder);
                        SearchRequest searchRequest = new SearchRequest();
                        searchRequest.source(searchSourceBuilder);
                        searchRequest.indices(queryEsIndex);
                        SearchResponse search = client.search(searchRequest, RequestOptions.DEFAULT);
                        for (SearchHit searchHit : search.getHits().getHits()) {
                            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                            sourceAsMap.put("esId", searchHit.getId());
                            sourceAsMap.put("dataSourceName", dataSourceName);
                            if (sourceAsMap.get("host") != null) {
                                String ip = sourceAsMap.get("host").toString();
                                List<String> assetsName = assetsIpInfoMap.get(ip);
                                List<Map> list = new ArrayList<>();
                                if (assetsName != null && assetsName.size() > 1) {
                                    for (String name : assetsName) {
                                        Map m = new HashMap();
                                        m.put("hostName", name);
                                        m.put("isOpen", false);
                                        list.add(m);
                                    }
                                }
                                if (assetsName != null && assetsName.size() > 0) {
                                    sourceAsMap.put("hostName", assetsName.get(0));
                                } else {
                                    sourceAsMap.put("hostName", ip);
                                }
                                sourceAsMap.put("hostNameList", list);
                            }
                            if (sourceAsMap.get("@timestamp") != null) {
                                String date = sourceAsMap.get("@timestamp").toString();
                                sourceAsMap.put("@timestamp", getUTCToCST(date));
                            }
                            sourceAsMap.put("isOpen", true);
                            listMap.add(sourceAsMap);
                        }
                        count = search.getHits().getTotalHits().value;
                    }
                }
            } else {
                return Reply.ok("数据源配置信息已失效，请重新配置");
            }
            setRuleByAllSysLog(listMap);
            if (param.getQueryFieldList() != null && param.getQueryFieldList().size() > 0) {
                for (EsQueryParam esQueryParam : param.getQueryFieldList()) {
                    //字符串
                    if ((!Strings.isNullOrEmpty(esQueryParam.getQueryField())) && ("dataSourceName".equals(esQueryParam.getQueryField())) && (!Strings.isNullOrEmpty(esQueryParam.getQueryValue()))) {
                        listMap = listMap.stream().filter(m -> m.get("dataSourceName").equals(esQueryParam.getQueryValue())).collect(Collectors.toList());
                    }
                }
            }
            PageInfo pageInfo = new PageInfo<>(listMap);
            pageInfo.setList(listMap);
            pageInfo.setTotal(count);
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("fail to getSystemLogInfos param{}, case by {}", param, e);
            return Reply.fail(500, "获取日志数据信息失败");
        }
    }


    private List<Map<String, Object>> setRuleByAllSysLog(List<Map<String, Object>> listMap) {
        //获取所有的规则列表数据
        List<EsSysLogRuleDTO> list = esSysLogRuleDao.getRulesInfosBySysLogAudit();
        for (EsSysLogRuleDTO esSysLogRuleDTO : list) {
            if (esSysLogRuleDTO != null && esSysLogRuleDTO.getId() != null) {
                //获取标签信息
                List<EsSysLogTagDTO> listTag = esSysLogRuleDao.getRuleTags(esSysLogRuleDTO.getId());
                esSysLogRuleDTO.setTagDTOList(listTag);
            }
            if (esSysLogRuleDTO != null && esSysLogRuleDTO.getRuleId() != null) {
                //获取规则信息
                List<MwRuleSelectParam> listRule = esSysLogRuleDao.getAlertRules(esSysLogRuleDTO.getRuleId());
                esSysLogRuleDTO.setMwRuleSelectListParam(listRule);
            }
        }
        //循环日志列表
        for (Map<String, Object> m : listMap) {
            MessageContext messageContext = new MessageContext();
            messageContext.setKey((HashMap) m);
            //循环规则列表
            for (EsSysLogRuleDTO esSysLogRuleDTO : list) {
                if (esSysLogRuleDTO != null && esSysLogRuleDTO.getMwRuleSelectListParam() != null &&
                        esSysLogRuleDTO.getMwRuleSelectListParam().size() != 0) {
                    //获取规则信息
                    List<MwRuleSelectParam> ruleSelectList = esSysLogRuleDTO.getMwRuleSelectListParam();
                    Boolean resultBoolean = false;
                    List<MwRuleSelectParam> ruleSelectParams = new ArrayList<>();
                    for (MwRuleSelectParam s : ruleSelectList) {
                        if (s.getParentKey().equals("root")) {
                            ruleSelectParams.add(s);
                        }
                    }
                    for (MwRuleSelectParam s : ruleSelectParams) {
                        s.setConstituentElements(getChild(s.getKey(), ruleSelectList));
                    }
                    resultBoolean = DelFilter.delFilter(ruleSelectParams, messageContext, ruleSelectList);
                    log.info("ruleSelectParams: " + ruleSelectParams);
                    log.info("messageContext:" + messageContext);
                    log.info("ruleSelectParams: " + ruleSelectParams);
                    log.info("result: " + resultBoolean);
                    if (resultBoolean) {
                        m.put("tagList", esSysLogRuleDTO.getTagDTOList());
                        break;
                    } else {
                        m.put("tagList", new ArrayList<>());
                    }
                }
            }
        }
        return listMap;
    }

    @Override
    public Reply fuzzSearchFiled(EsSysLogAuditQueryDTO param) {
        Map<String, List> maps = new HashMap();
        try {
            Date endTime = new Date();
            Date startTime = DateUtils.addMinutes(endTime, -10);
            if (param.getDateType() != null) {
                switch (param.getDateType()) {//1:10分钟、2:1小时、3:12小时、4:1天、5:一周 6:一个月 7：自定义
                    case 1:
                        startTime = DateUtils.addMinutes(endTime, -10);
                        break;
                    case 2:
                        startTime = DateUtils.addHours(endTime, -1);
                        break;
                    case 3:
                        startTime = DateUtils.addHours(endTime, -12);
                        break;
                    case 4:
                        startTime = DateUtils.addDays(endTime, -1);
                        break;
                    case 5:
                        startTime = DateUtils.addDays(endTime, -7);
                        break;
                    case 6:
                        startTime = DateUtils.addMonths(endTime, -1);
                        break;
                    case 7:
                        if (param.getStartTime() == null || param.getEndTime() == null) {
                            return Reply.fail("自定义时间不可为空");
                        }
                        startTime = param.getStartTime();
                        endTime = param.getEndTime();
                        break;
                    default:
                        break;
                }
            }
            EsDataSourceListDto dto = new EsDataSourceListDto();
            if (dto != null && dto.getInfoList() != null && dto.getInfoList().size() > 0) {
                for (EsDataSourceListInfoDto p : dto.getInfoList()) {
                    //暂时日志来源于es
                    if (ELASTICSEARCH.getType().equals(p.getDataSourceType())) {
                        RestHighLevelClient client = p.getClient();
                        String queryEsIndex = p.getQueryEsIndex();
                        //数据源名称
                        String dataSourceName = p.getDataSourceName();
                        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                        TermsAggregationBuilder aggregationBuilder = null;
                        //对字段进行桶分组
                        for (String field : listStr) {
                            aggregationBuilder = AggregationBuilders.terms(field).field(field + ".keyword").size(scrollSize);
                            searchSourceBuilder.aggregation(aggregationBuilder);
                        }
                        //不输出原始数据
                        searchSourceBuilder.from(0);
                        searchSourceBuilder.size(scrollSize);
                        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
                        queryBuilder.must(QueryBuilders.rangeQuery("@timestamp").from(MWUtils.getUtcTime(DateUtils.formatDateTime(startTime))).to(MWUtils.getUtcTime(DateUtils.formatDateTime(endTime))));
                        searchSourceBuilder.query(queryBuilder);
                        //打印dsl语句
                        log.info("dsl:" + searchSourceBuilder.toString());
                        //设置索引以及填充语句
                        SearchRequest searchRequest = new SearchRequest();
                        searchRequest.indices("syslog*");
                        searchRequest.source(searchSourceBuilder);
                        SearchResponse response = null;
                        response = client.search(searchRequest, RequestOptions.DEFAULT);
                        //解析数据，获取host_tr的指标聚合参数。
                        Aggregations aggregations = response.getAggregations();
                        Map map = aggregations.getAsMap();
                        map.forEach((k, v) -> {
                            List<String> list1 = new ArrayList<>();
                            String name = k.toString();
                            ParsedStringTerms parsedStringTerms = (ParsedStringTerms) v;
                            List<? extends Terms.Bucket> buckets = parsedStringTerms.getBuckets();
                            for (Terms.Bucket bucket : buckets) { //key的数据
                                String key = bucket.getKey().toString();
                                long docCount = bucket.getDocCount(); //获取数据
                                list1.add(key);
                            }
                            maps.put(name, list1);
                        });
                        maps.put("dataSourceName", Arrays.asList(dataSourceName));
                    }
                }
            }
            return Reply.ok(maps);
        } catch (Exception e) {
            log.error("fail to getSystemLogInfos param{}, case by {}", param, e);
            return Reply.fail(500, "获取日志数据信息失败");
        }
    }

    @Override
    public Reply sysLogExport(EsSysLogAuditQueryDTO param, HttpServletRequest request, HttpServletResponse response) {
        try {
            EsDataSourceListDto dto = new EsDataSourceListDto();
            List<Map<String, Object>> assetsIpInfo = new ArrayList<>();
            if (modelAssetEnable) {//获取资源中心下的资产数据
                assetsIpInfo = mwModelViewCommonService.getModelListInfoByPerm(new QueryModelAssetsParam());

            } else {//获取mw_tangibleassets_table表中的数据
                assetsIpInfo = esSysLogAuditDao.getAllAssetsInfoByIp();
            }
            Map<String, List> assetsIpInfoMap = new HashMap();
            for (Map<String, Object> m : assetsIpInfo) {
                String ip = "";
                String name = "";
                if (m.get("inBandIp") != null) {
                    ip = m.get("inBandIp").toString();
                }
                if (m.get("instanceName") != null) {
                    name = m.get("instanceName").toString();
                }
                if (assetsIpInfoMap.containsKey(ip)) {
                    List<String> listName = assetsIpInfoMap.get(ip);
                    listName.add(name);
                    assetsIpInfoMap.put(ip, listName);
                } else {
                    List<String> listName = new ArrayList<>();
                    listName.add(name);
                    assetsIpInfoMap.put(ip, listName);
                }
            }
            List<Map> listMap = new ArrayList<>();
            Date endTime = new Date();
            Date startTime = DateUtils.addMinutes(endTime, -10);
            if (param.getDateType() != null) {
                switch (param.getDateType()) {//1:10分钟、2:1小时、3:12小时、4:1天、5:一周 6:一个月 7：自定义
                    case 1:
                        startTime = DateUtils.addMinutes(endTime, -10);
                        break;
                    case 2:
                        startTime = DateUtils.addHours(endTime, -1);
                        break;
                    case 3:
                        startTime = DateUtils.addHours(endTime, -12);
                        break;
                    case 4:
                        startTime = DateUtils.addDays(endTime, -1);
                        break;
                    case 5:
                        startTime = DateUtils.addDays(endTime, -7);
                        break;
                    case 6:
                        startTime = DateUtils.addMonths(endTime, -1);
                        break;
                    case 7:
                        if (param.getStartTime() == null || param.getEndTime() == null) {
                            return Reply.fail("自定义时间不可为空");
                        }
                        startTime = param.getStartTime();
                        endTime = param.getEndTime();
                        break;
                    default:
                        break;
                }
            }
            long count = 0l;
            if (dto != null && dto.getInfoList() != null && dto.getInfoList().size() > 0) {
                for (EsDataSourceListInfoDto p : dto.getInfoList()) {
                    //暂时日志来源于es
                    if (ELASTICSEARCH.getType().equals(p.getDataSourceType())) {
                        RestHighLevelClient client = p.getClient();
                        String queryEsIndex = p.getQueryEsIndex();
                        //数据源名称
                        String dataSourceName = p.getDataSourceName();

                        //条件组合查询
                        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
                        queryBuilder.must(QueryBuilders.rangeQuery("@timestamp").from(MWUtils.getUtcTime(DateUtils.formatDateTime(startTime))).to(MWUtils.getUtcTime(DateUtils.formatDateTime(endTime))));
                        //全字段模糊查询
                        if (param.getQueryFieldList() != null && param.getQueryFieldList().size() > 0) {
                            BoolQueryBuilder queryBuilder2 = QueryBuilders.boolQuery();
                            for (EsQueryParam esQueryParam : param.getQueryFieldList()) {
                                //字符串
                                if ((!Strings.isNullOrEmpty(esQueryParam.getQueryField())) && (!Strings.isNullOrEmpty(esQueryParam.getQueryValue()))) {
                                    queryBuilder2 = queryBuilder2.should(QueryBuilders.wildcardQuery(esQueryParam.getQueryField() + ".keyword", "*" + (esQueryParam.getQueryValue().trim().replaceFirst(" ", "*")) + "*"));
                                }
                            }
                            queryBuilder.must(queryBuilder2);
                        }
                        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                        searchSourceBuilder.from((param.getPageNumber() - 1) * param.getPageSize());
                        searchSourceBuilder.size(scrollSize);
                        //返回指定字段数据
                        String[] includes = fieldList.toArray(new String[fieldList.size()]);
                        FetchSourceContext sourceContext = new FetchSourceContext(true, includes, null);
                        searchSourceBuilder.fetchSource(sourceContext);
                        //排序
                        //字符串的排序需要使用不分词类型
                        if (!Strings.isNullOrEmpty(param.getSortField())) {
                            //时间排序
                            if ("@timestamp".equals(param.getSortField())) {
                                searchSourceBuilder.sort("@timestamp", (param.getSortType() == 1 ? SortOrder.DESC : SortOrder.ASC));
                            } else {//字符串排序
                                searchSourceBuilder.sort(param.getSortField() + (".keyword"), (param.getSortType() == 1 ? SortOrder.DESC : SortOrder.ASC));
                            }
                        }
                        //设置超时时间
                        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
                        searchSourceBuilder.query(queryBuilder);
                        SearchRequest searchRequest = new SearchRequest();
                        searchRequest.source(searchSourceBuilder);
                        searchRequest.indices(queryEsIndex);
                        SearchResponse search = client.search(searchRequest, RequestOptions.DEFAULT);
                        for (SearchHit searchHit : search.getHits().getHits()) {
                            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                            sourceAsMap.put("esId", searchHit.getId());
                            sourceAsMap.put("dataSourceName", dataSourceName);
                            if (sourceAsMap.get("host") != null) {
                                String ip = sourceAsMap.get("host").toString();
                                List<String> assetsName = assetsIpInfoMap.get(ip);
                                List<Map> list = new ArrayList<>();
                                if (assetsName != null && assetsName.size() > 0) {
                                    sourceAsMap.put("hostName", assetsName.get(0));
                                } else {
                                    sourceAsMap.put("hostName", ip);
                                }
                            }
                            if (sourceAsMap.get("@timestamp") != null) {
                                String date = sourceAsMap.get("@timestamp").toString();
                                sourceAsMap.put("@timestamp", getUTCToCST(date));
                            }
                            listMap.add(sourceAsMap);
                        }
                    }
                }
            }
            List<String> lable = Arrays.asList("hostName", "host", "severity_label", "facility_label", "@timestamp", "message", "dataSourceName");
            List<String> lableName = Arrays.asList("资产名称", "IP地址", "级别", "类型", "时间", "信息", "数据源");
            ExportExcel.exportExcel("日志审计导出", "日志审计导出", lableName, lable, listMap, "yyyy-MM-dd HH:mm:ss", response);
        } catch (Exception e) {
            log.error("fail to sysLogExport param{}, case by {}", param, e);
        }
        return Reply.ok("导出成功");
    }


    /**
     * 获取系统日志数量
     *
     * @param param
     * @return
     */
    @Override
    public Reply getSystemLogNums(EsSysLogAuditQueryDTO param) {
        List<Map> list = new ArrayList<>();
        Date endTime = new Date();
        Date startTime = DateUtils.addMinutes(endTime, -10);
        if (param.getDateType() != null) {
            switch (param.getDateType()) {//1:10分钟、2:1小时、3:12小时、4:1天、5:一周 6:一个月 7：自定义
                case 1:
                    startTime = DateUtils.addMinutes(endTime, -10);
                    break;
                case 2:
                    startTime = DateUtils.addHours(endTime, -1);
                    break;
                case 3:
                    startTime = DateUtils.addHours(endTime, -12);
                    break;
                case 4:
                    startTime = DateUtils.addDays(endTime, -1);
                    break;
                case 5:
                    startTime = DateUtils.addDays(endTime, -7);
                    break;
                case 6:
                    startTime = DateUtils.addMonths(endTime, -1);
                    break;
                case 7:
                    if (param.getStartTime() == null || param.getEndTime() == null) {
                        return Reply.fail("自定义时间不可为空");
                    }
                    startTime = param.getStartTime();
                    endTime = param.getEndTime();
                    break;
                default:
                    break;
            }
            List<String> dateList = dateSubSpilt(DateUtils.formatDateTime(startTime), DateUtils.formatDateTime(endTime), 30);
            EsDataSourceListDto dto = new EsDataSourceListDto();
            List<EsDataSourceListInfoDto> listInfo = dto.getInfoList();
            long time1 = System.currentTimeMillis();
            long time2 = 0l;
            long time3 = 0l;
            for (EsDataSourceListInfoDto p : listInfo) {
                //暂时日志来源于es
                if (ELASTICSEARCH.getType().equals(p.getDataSourceType())) {
                    Map map = new HashMap();
                    RestHighLevelClient client = p.getClient();
                    String queryEsIndex = p.getQueryEsIndex();
                    //数据源名称
                    String dataSourceName = p.getDataSourceName();
                    time2 = System.currentTimeMillis();
                    //根据时间对系统日志数量统计
                    List<Map> mapList = getEsSysLogNumByTime(param, queryEsIndex, dataSourceName, dateList, client);
                    map.put("name", dataSourceName);
                    map.put("data", mapList);
                    list.add(map);
                    time3 = System.currentTimeMillis();
                }
            }
            long time4 = System.currentTimeMillis();
            //System.out.printf("时间1：" + (time2 - time1) + "；时间2：" + (time3 - time2) + "；时间3：" + (time4 - time3) + "；总时间：" + (time4 - time1));
        }
        return Reply.ok(list);
    }

    /**
     * 系统日志数量统计
     *
     * @param queryEsIndex
     * @param dataSourceName
     * @param dateList
     * @param client
     * @return
     */
    public List<Map> getEsSysLogNumByTime(EsSysLogAuditQueryDTO param, String queryEsIndex, String
            dataSourceName, List<String> dateList, RestHighLevelClient client) {
        List<Map> list = new ArrayList<>();
        List<Future<Map>> futureList = new ArrayList<>();
        List<Map> listInfo = new ArrayList<>();
        try {
            int coreSizePool = 15;
            ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(15, 18, 10, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
            for (int i = 0; i < dateList.size() - 1; i++) {
                Map map = new HashMap();
                BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
                queryBuilder.must(QueryBuilders.rangeQuery("@timestamp").from(MWUtils.getUtcTime(dateList.get(i))).to(MWUtils.getUtcTime(dateList.get(i + 1))));
                //全字段模糊查询
                if (param.getQueryFieldList() != null && param.getQueryFieldList().size() > 0) {
                    BoolQueryBuilder queryBuilder2 = QueryBuilders.boolQuery();
                    for (EsQueryParam esQueryParam : param.getQueryFieldList()) {
                        //字符串
                        if ((!Strings.isNullOrEmpty(esQueryParam.getQueryField())) && (!Strings.isNullOrEmpty(esQueryParam.getQueryValue()))) {
                            queryBuilder2 = queryBuilder2.should(QueryBuilders.wildcardQuery(esQueryParam.getQueryField() + ".keyword", "*" + (esQueryParam.getQueryValue().trim().replaceFirst(" ", "*")) + "*"));
                        }
                    }
                    queryBuilder.must(queryBuilder2);
                }
                CountRequest countRequest = new CountRequest();
                // 绑定索引名
                countRequest.indices(queryEsIndex);
                countRequest.query(queryBuilder);
                final int x = i;
                Callable<Map> callable = new Callable<Map>() {
                    @Override
                    public Map call() throws Exception {
                        Map m = new HashMap();
                        if (null != queryBuilder) { // 通过CountRequest查询获得count
                            CountResponse response = client.count(countRequest, RequestOptions.DEFAULT);
                            long count = response.getCount();
                            //System.out.println("数量：" + count + "；时间：" + dateList.get(x) + "次数：" + x);
                            m.put("date", dateList.get(x));
                            m.put("count", count);
                            m.put("dataSourceName", dataSourceName);
                        }
                        return m;
                    }
                };
                Future<Map> submit = threadPoolExecutor.submit(callable);
                futureList.add(submit);
//                map.put(dateList.get(i), count);
//                map.put("date", dateList.get(i));
//                map.put("count", count);
//                map.put("dataSourceName", dataSourceName);
//                list.add(map);
            }
            if (futureList.size() > 0) {
                futureList.forEach(f -> {
                    try {
                        Map result = f.get(20, TimeUnit.SECONDS);
                        listInfo.add(result);
                    } catch (Exception e) {
                        log.error("fail to getEsSysLogNumByTime:多线程等待数据返回失败 param:{},cause:{}", param, e);
                    }
                });
            }
            threadPoolExecutor.shutdown();
            log.info("关闭线程池");
        } catch (Exception e) {
            log.error("fail to getEsSysLogNumByTime param{}, case by {}", param, e);
        }
        return listInfo;
    }

    public Reply getSysLogTree(EsSysLogAuditQueryDTO param) {
        List<Map> list = new ArrayList<>();
        Date endTime = new Date();
        Date startTime = DateUtils.addMinutes(endTime, -10);
        List<Map> listMap = new ArrayList<>();
        try {
            if (param.getDateType() != null) {
                switch (param.getDateType()) {//1:10分钟、2:1小时、3:12小时、4:1天、5:一周 6:一个月 7：自定义
                    case 1:
                        startTime = DateUtils.addMinutes(endTime, -10);
                        break;
                    case 2:
                        startTime = DateUtils.addHours(endTime, -1);
                        break;
                    case 3:
                        startTime = DateUtils.addHours(endTime, -12);
                        break;
                    case 4:
                        startTime = DateUtils.addDays(endTime, -1);
                        break;
                    case 5:
                        startTime = DateUtils.addDays(endTime, -7);
                        break;
                    case 6:
                        startTime = DateUtils.addMonths(endTime, -1);
                        break;
                    case 7:
                        if (param.getStartTime() == null || param.getEndTime() == null) {
                            return Reply.fail("自定义时间不可为空");
                        }
                        startTime = param.getStartTime();
                        endTime = param.getEndTime();
                        break;
                    default:
                        break;
                }
            }

            EsDataSourceListDto dto = new EsDataSourceListDto();
            if (dto != null && dto.getInfoList() != null && dto.getInfoList().size() > 0) {
                for (EsDataSourceListInfoDto p : dto.getInfoList()) {
                    //暂时日志来源于es
                    if (ELASTICSEARCH.getType().equals(p.getDataSourceType())) {
                        RestHighLevelClient client = p.getClient();
                        String queryEsIndex = p.getQueryEsIndex();
                        //数据源名称
                        String dataSourceName = p.getDataSourceName();
                        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                        TermsAggregationBuilder aggregationBuilder = null;
                        //对字段进行桶分组
                        for (String field : listStr) {
                            aggregationBuilder = AggregationBuilders.terms(field).field(field + ".keyword").size(scrollSize);
                            searchSourceBuilder.aggregation(aggregationBuilder);
                        }
                        //不输出原始数据
                        searchSourceBuilder.from(0);
                        searchSourceBuilder.size(scrollSize);
                        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
                        queryBuilder.must(QueryBuilders.rangeQuery("@timestamp").from(MWUtils.getUtcTime(DateUtils.formatDateTime(startTime))).to(MWUtils.getUtcTime(DateUtils.formatDateTime(endTime))));
                        searchSourceBuilder.query(queryBuilder);
                        //打印dsl语句
                        log.info("dsl:" + searchSourceBuilder.toString());
                        //设置索引以及填充语句
                        SearchRequest searchRequest = new SearchRequest();
                        searchRequest.indices("syslog*");
                        searchRequest.source(searchSourceBuilder);
                        SearchResponse response = null;
                        response = client.search(searchRequest, RequestOptions.DEFAULT);
                        //解析数据，获取host_tr的指标聚合参数。
                        Aggregations aggregations = response.getAggregations();
                        Map map = aggregations.getAsMap();
                        map.forEach((k, v) -> {
                            Map maps = new HashMap();
                            List<Map> list1 = new ArrayList<>();
                            String name = k.toString();
                            ParsedStringTerms parsedStringTerms = (ParsedStringTerms) v;
                            List<? extends Terms.Bucket> buckets = parsedStringTerms.getBuckets();
                            for (Terms.Bucket bucket : buckets) { //key的数据
                                Map map1 = new HashMap();
                                String key = bucket.getKey().toString();
                                long docCount = bucket.getDocCount(); //获取数据
                                map1.put("name", key);
                                map1.put("num", docCount);
                                map1.put("parentName", name);
                                list1.add(map1);
                            }
                            if ("host".equals(name)) {
                                name = "Host";
                            }
                            if ("type".equals(name)) {
                                name = "Type";
                            }
                            if ("severity_label".equals(name)) {
                                name = "Severity";
                            }
                            if ("facility_label".equals(name)) {
                                name = "Facility";
                            }
                            maps.put("name", name);
                            maps.put("info", list1);
                            maps.put("size", buckets.size());
                            list.add(maps);
                        });
                    }
                }
            }
            Map<String, Map> maps = new HashMap();
            for (Map m : list) {//对多数据源的树结构进行合并
                Map mapInfo = new HashMap();
                if (maps.containsKey(m.get("name").toString())) {
                    Map mp = maps.get(m.get("name").toString());
                    Integer size = Integer.valueOf(mp.get("size").toString()) + Integer.valueOf(m.get("size").toString());
                    List info = (List) mp.get("info");
                    info.addAll((List) m.get("info"));
                    mapInfo.put("size", size);
                    mapInfo.put("name", m.get("name").toString());
                    mapInfo.put("info", info);
                    maps.put(m.get("name").toString(), mapInfo);
                } else {
                    maps.put(m.get("name").toString(), m);
                }
            }
            maps.forEach((k, v) -> {
                listMap.add(v);
            });
        } catch (Exception e) {
            log.error("fail to getSysLogTree param{}, case by {}", param, e);
            return Reply.fail(500, "获取日志资源树失败");
        }
        return Reply.ok(listMap);
    }


    /**
     * 对时间段进行等分切割
     *
     * @param startTime 开始时间
     * @param endTIme   结束时间
     * @param sum       等分个数
     * @return
     */
    public List<String> dateSubSpilt(String startTime, String endTIme, int sum) {
        long timestamp = DateUtils.between(startTime, endTIme, DateUnitEnum.MS);
        List<String> dateStrList = new ArrayList<>();
        if (sum != 0) {
            long timeInterval = timestamp / sum;
            for (int i = 0; i <= sum; i++) {
                long time = DateUtils.parse(startTime).getTime() + (timeInterval * (i));
                Date startTime1 = new Date(time);
                dateStrList.add(DateUtils.formatDateTime(startTime1));
            }
            Collections.sort(dateStrList);
        }
        return dateStrList;
    }

    @Override
    public Reply initDataSourceState() {
        List<DataSourceConfigureDTO> list = esSysLogAuditDao.initDataSourceState();
        /*List<Long> ids = new ArrayList<>();
        for (DataSourceConfigureDTO dataSourceConfigureDTO : list) {
            ids.add(dataSourceConfigureDTO.getId());
        }
        DataSourceConfigureDTO dto = new DataSourceConfigureDTO();
        dto.setIds(ids);
        dataSourceImpl.initConfig(dto);*/
        Map<String, Integer> idsAndTypes = new HashMap<>();
        for (DataSourceConfigureDTO dataSourceConfigureDTO : list) {
            idsAndTypes.put(dataSourceConfigureDTO.getId(), dataSourceConfigureDTO.getDataSourceType());
        }
        DataSourceConfigureDTO dto = new DataSourceConfigureDTO();
        dto.setIdsAndTypes(idsAndTypes);
        dataSourceImpl.initConfig(dto);
        return Reply.ok();
    }

    protected List<MwRuleSelectParam> getChild(String key, List<MwRuleSelectParam> rootList) {
        List<MwRuleSelectParam> childList = new ArrayList<>();
        for (MwRuleSelectParam s : rootList) {
            if (s.getParentKey().equals(key)) {
                childList.add(s);
            }
        }
        for (MwRuleSelectParam s : childList) {
            s.setConstituentElements(getChild(s.getKey(), rootList));
        }
        if (childList.size() == 0) {
            return null;
        }
        return childList;
    }


//    public static void main(String[] args) {
//
//        try {
//            String strRSAPublicKey = "CAF0D754B61DC6CB37EFDED81A0A15397C89E01870FFB90DCE4D8DD4C050A1C9461447E164B1B07595EBFEDBF5B4FF50FE4EB0189B40CDE8970FBF8DE739C4B2442E90C44D914F5402BCCC5D892680D67D6D98740FCAE77E26D4DF22C5DEC00C180DC99373B71AB3BD63EBCD9F0E7447E27433DBA2AAEC756B917F12994B0E1918E55EAB55808F56CD823A9860A5C2F89953AEE748F39136B399BA8ABFC38FF2D7E82951945BB44BD5CEBB7A247963AD3B7FC5508CA227F07D29567CBAAF40F06C9642D6024D29CD3A378F77BC87A387EECDAD0DDC1913CCB52D3CA84AED53B5F36E6923550EED91D07FD3206376B3AD01413D8D41B2931E9BDABDB402B41A8F";
//            String exponent = "10001";
////            // 公钥指数
//            String plaintext = "admin123";
////            // 转换十六进制字符串为大整数
////            BigInteger modulus = new BigInteger(strRSAPublicKey, 16);
////            BigInteger exp = new BigInteger(exponent, 16);
////            // 创建 RSA 公钥对象
////            RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(modulus, exp);
////            KeyFactory keyFactory = null;
////
////            keyFactory = KeyFactory.getInstance("RSA");
////
////            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
////            // 使用公钥进行加密
////            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
////            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
////            byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
////            // 将加密结果转换为 Base64 编码字符串
////            String encryptedText = Base64.getEncoder().encodeToString(encryptedBytes);
////            System.out.println("加密后的数据：" + encryptedText);
//
//
//            // 转换公钥字符串为大整数
//            byte[] publicKeyBytes = Base64.getDecoder().decode(strRSAPublicKey);
//            BigInteger modulus = new BigInteger(1, publicKeyBytes);
//            // 创建RSA公钥规格
//            RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(modulus, new BigInteger(exponent));
//            // 生成RSA公钥对象
//            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
//            // 使用公钥进行加密
//            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
//            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
//            byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
//            // 将加密结果转换为Base64编码字符串
//            String encryptedText = Base64.getEncoder().encodeToString(encryptedBytes);
//            System.out.println("加密后的数据：" + encryptedText);
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
