package cn.mw.module.security.service.impl;

import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.StringUtils;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.module.security.dto.AddEsLogParam;
import cn.mw.module.security.dto.EslogParam;
import cn.mw.module.security.dto.EslogUpdateParam;
import cn.mw.module.security.dto.MessageParam;
import cn.mw.module.security.service.EslogService;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.security.dao.EslogDao;
import cn.mw.monitor.service.assets.model.MwCommonAssetsDto;
import cn.mw.monitor.service.user.api.MWOrgCommonService;
import cn.mw.monitor.util.MWUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author xhy
 * @date 2020/9/7 15:25
 */
@Service
@Slf4j
@ConditionalOnProperty(prefix = "elasticsearch", name = "enable", havingValue = "true")
public class EslogServiceImpl implements EslogService {
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Resource
    private EslogDao eslogDao;

    @Autowired
    private MwAssetsManager mwAssetsManager;

    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;

    @Autowired
    private MWOrgCommonService mwOrgCommonService;

    @Override
    public Reply getLogList(EslogParam param) {
        try {
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.from((param.getPageNumber() - 1) * param.getPageSize());
            searchSourceBuilder.size(param.getPageSize());
            searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
            searchSourceBuilder.sort(new FieldSortBuilder("logDate").order(SortOrder.DESC));


            if (null == param.getQueryDate() || param.getQueryDate().length == 0) {
                Calendar calendar = Calendar.getInstance();
                String[] queryDate = new String[2];
                queryDate[0] = MWUtils.getSolarData(0, 0, 0, "yyyy-MM-dd HH:mm:ss", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                queryDate[1] = MWUtils.getDate(new Date(), "yyyy-MM-dd HH:mm:ss");
                param.setQueryDate(queryDate);
            }
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
                    .must(QueryBuilders.rangeQuery("logDate").from(MWUtils.getUtcTime(param.getQueryDate()[0])).to(MWUtils.getUtcTime(param.getQueryDate()[1])));
            searchSourceBuilder.query(queryBuilder);
            if (StringUtils.isNotEmpty(param.getIp())) {
                searchSourceBuilder.query(QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery("logDate").from(MWUtils.getUtcTime(param.getQueryDate()[0])).to(MWUtils.getUtcTime(param.getQueryDate()[1])))
                        .must(QueryBuilders.matchQuery("ip", param.getIp()).fuzziness(Fuzziness.AUTO)));//设置字段模糊查询 默认最少要传入三个字符才能进行查询
            }
            if (StringUtils.isNotEmpty(param.getEventName())) {
                searchSourceBuilder.query(QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery("logDate").from(MWUtils.getUtcTime(param.getQueryDate()[0])).to(MWUtils.getUtcTime(param.getQueryDate()[1])))
                        .must(QueryBuilders.matchQuery("eventName", param.getEventName()).fuzziness(Fuzziness.AUTO)));//设置字段模糊查询
            }
            if (StringUtils.isNotEmpty(param.getSourceIp())) {
                searchSourceBuilder.query(QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery("logDate").from(MWUtils.getUtcTime(param.getQueryDate()[0])).to(MWUtils.getUtcTime(param.getQueryDate()[1])))
                        .must(QueryBuilders.matchQuery("sourceIp", param.getSourceIp()).fuzziness(Fuzziness.AUTO)));//设置字段模糊查询
            }
            if (StringUtils.isNotEmpty(param.getSourcePort())) {
                searchSourceBuilder.query(QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery("logDate").from(MWUtils.getUtcTime(param.getQueryDate()[0])).to(MWUtils.getUtcTime(param.getQueryDate()[1])))
                        .must(QueryBuilders.matchQuery("sourcePort", param.getSourcePort()).fuzziness(Fuzziness.AUTO)));//设置字段模糊查询
            }
            if (StringUtils.isNotEmpty(param.getDestIp())) {
                searchSourceBuilder.query(QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery("logDate").from(MWUtils.getUtcTime(param.getQueryDate()[0])).to(MWUtils.getUtcTime(param.getQueryDate()[1])))
                        .must(QueryBuilders.matchQuery("destIp", param.getDestIp()).fuzziness(Fuzziness.AUTO)));//设置字段模糊查询
            }
            if (StringUtils.isNotEmpty(param.getDestPort())) {
                searchSourceBuilder.query(QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery("logDate").from(MWUtils.getUtcTime(param.getQueryDate()[0])).to(MWUtils.getUtcTime(param.getQueryDate()[1])))
                        .must(QueryBuilders.matchQuery("destPort", param.getDestPort()).fuzziness(Fuzziness.AUTO)));//设置字段模糊查询
            }
            if (StringUtils.isNotEmpty(param.getOccurCount())) {
                searchSourceBuilder.query(QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery("logDate").from(MWUtils.getUtcTime(param.getQueryDate()[0])).to(MWUtils.getUtcTime(param.getQueryDate()[1])))
                        .must(QueryBuilders.matchQuery("occurCount", param.getOccurCount()).fuzziness(Fuzziness.AUTO)));//设置字段模糊查询
            }
            if (StringUtils.isNotEmpty(param.getResult())) {
                searchSourceBuilder.query(QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery("logDate").from(MWUtils.getUtcTime(param.getQueryDate()[0])).to(MWUtils.getUtcTime(param.getQueryDate()[1])))
                        .must(QueryBuilders.matchQuery("result", param.getResult()).fuzziness(Fuzziness.AUTO)));//设置字段模糊查询
            }
            if (StringUtils.isNotEmpty(param.getDevice())) {
                searchSourceBuilder.query(QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery("logDate").from(MWUtils.getUtcTime(param.getQueryDate()[0])).to(MWUtils.getUtcTime(param.getQueryDate()[1])))
                        .must(QueryBuilders.matchQuery("device", param.getDevice()).fuzziness(Fuzziness.AUTO)));//设置字段模糊查询
            }
            if (StringUtils.isNotEmpty(param.getDisposalSuggestion())) {
                searchSourceBuilder.query(QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery("logDate").from(MWUtils.getUtcTime(param.getQueryDate()[0])).to(MWUtils.getUtcTime(param.getQueryDate()[1])))
                        .must(QueryBuilders.matchQuery("disposalSuggestion", param.getDisposalSuggestion()).fuzziness(Fuzziness.AUTO)));//设置字段模糊查询
            }
            if (StringUtils.isNotEmpty(param.getDisposalUser())) {
                searchSourceBuilder.query(QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery("logDate").from(MWUtils.getUtcTime(param.getQueryDate()[0])).to(MWUtils.getUtcTime(param.getQueryDate()[1])))
                        .must(QueryBuilders.matchQuery("disposalUser", param.getDisposalUser()).fuzziness(Fuzziness.AUTO)));//设置字段模糊查询
            }
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.source(searchSourceBuilder);
            try {
                SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

                List<Map<String, Object>> list = new ArrayList<>();
                for (SearchHit searchHit : search.getHits().getHits()) {
                    Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                    if (null != sourceAsMap.get("destIp") && StringUtils.isNotEmpty(sourceAsMap.get("destIp").toString())) {
                        String ip = sourceAsMap.get("destIp").toString();
                        List<String> orgNameByIp = eslogDao.getOrgNameByIp(ip);
                        if (orgNameByIp.size() > 0) {
                            sourceAsMap.put("orgName", orgNameByIp.toString());
                        }
                        //List<String> orgNameByLoginName = mwUserOrgMapperDao.getOrgNameByLoginName(iLoginCacheInfo.getLoginName());
                        List<String> orgNameByLoginName = mwOrgCommonService.getOrgNamesByNodes(iLoginCacheInfo.getLoginName());

                        if (orgNameByIp.size() > 0) {
                            sourceAsMap.put("reportOrg", orgNameByLoginName.toString());
                        }
                    }
                    sourceAsMap.put("id", searchHit.getId());
                    sourceAsMap.put("index", searchHit.getIndex());
                    list.add(sourceAsMap);
                }
                long count = search.getHits().getTotalHits().value;
                PageInfo pageInfo = new PageInfo<>(list);
                pageInfo.setList(list);
                pageInfo.setTotal(count);
                return Reply.ok(pageInfo);
            } catch (ElasticsearchException e) {//索引不存在
                PageInfo pageInfo = new PageInfo<>();
                return Reply.ok(pageInfo);
            }
        } catch (Exception e) {
            log.error("fail to getLogList with d={}, cause:{}", param, e);
            return Reply.fail(ErrorConstant.LOGGER_BROWSE_CODE_310001, ErrorConstant.LOGGER_BROWSE_MSG_310001);
        }
    }

    @Override
    public Reply updateLogList(EslogUpdateParam param) {
        try {
            UpdateRequest updateRequest = new UpdateRequest(param.getIndex(), param.getId());
            updateRequest.timeout(new TimeValue(60, TimeUnit.SECONDS));
            updateRequest.doc(XContentFactory.jsonBuilder()
                    .startObject()
                    .field("disposalUser", iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getLoginInfo().getUser().getUserName() + "(" + iLoginCacheInfo.getLoginName() + ")")
                    .field("disposalDate", MWUtils.getDate(new Date(), "yyyy-MM-dd HH:MM:ss"))
                    .field("disposalStatus", "1")
                    .field("disposalSuggestion", param.getDisposalSuggestion())
                    .field("alertLevel", param.getAlertLevel())
                    .field("destDomain", param.getDestDomain())
                    .field("isRecord", param.getIsRecord())
                    .field("isTest", param.getIsTest())
                    .endObject());
            UpdateResponse update = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
            RestStatus status = update.status();
            if (status.getStatus() == 200) {
                //  restHighLevelClient.close();
                return Reply.ok();
            } else {
                return Reply.fail(ErrorConstant.LOGGER_UPDATE_CODE_310002, ErrorConstant.LOGGER_UPDATE_MSG_310002);
            }
        } catch (Exception e) {
            log.error("fail to updateLogList with d={}, cause:{}", param, e);
            return Reply.fail(ErrorConstant.LOGGER_UPDATE_CODE_310002, ErrorConstant.LOGGER_UPDATE_MSG_310002);
        }
    }

    @Override
    public Reply creatLog(AddEsLogParam param) {
        try {
            String todayDate = MWUtils.getSolarData(0, 0, 0, "yyyy.MM.dd", 0);
            String index = "custom" + todayDate + ".log";
            GetIndexRequest request = new GetIndexRequest(index);//创建索引
            boolean exists = restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
            if (!exists) {
                CreateIndexRequest createIndexRequest = new CreateIndexRequest(index);//创建索引
                CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
            }
            param.setEventName("自主上报攻击事件");
          //  List<String> orgNameByLoginName = mwUserOrgMapperDao.getOrgNameByLoginName(iLoginCacheInfo.getLoginName());
            List<String> orgNameByLoginName = mwOrgCommonService.getOrgNamesByNodes(iLoginCacheInfo.getLoginName());

            param.setReportOrg(orgNameByLoginName.toString());
            AddEsLogParam eslogDto = AddEsLogParam.builder()
                    .alertLevel(param.getAlertLevel())
                    .reportOrg(param.getReportOrg())
                    .eventType(param.getEventType())//攻击类型
                    .message(JSON.toJSONString(param))
                    .disposalSuggestion(param.getDisposalSuggestion())
                    .disposalStatus(param.getDisposalStatus())
                    .ip("")
                    .device(param.getDevice())
                    .eventName(param.getEventName())
                    .sourceIp(param.getSourceIp())
                    .sourcePort(param.getSourcePort())
                    .destIp(param.getDestIp())
                    .destPort(param.getDestPort())
                    .destDomain(param.getDestDomain())
                    .logDate(param.getLogDate())
                    .isRecord(param.getIsRecord())//备案
                    .isTest(param.getIsTest())//测评
                    .disposalType(param.getDisposalType())
                    .code("")
                    .priorityLevel("")
                    .result("")
                    .occurCount(param.getOccurCount())
                    .type("syslog-custom")
                    .build();
            JSONObject json = (JSONObject) JSONObject.toJSON(eslogDto);
            json.put("@timestamp", new Date());
            IndexRequest newrequest = new IndexRequest(index).source(json);
            try {

                IndexResponse response = restHighLevelClient.index(newrequest, RequestOptions.DEFAULT);
                log.info(response.getId());
                log.info("成功");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return Reply.ok();
        } catch (Exception e) {
            log.error("fail to updateLogList with d={}, cause:{}", param, e);
            return Reply.fail(ErrorConstant.LOGGER_CREATE_CODE_310003, ErrorConstant.LOGGER_CREATE_MSG_310003);
        }
    }


    @Override
    public Reply getMessageList(MessageParam param) {

        SearchHit errorSearchHit = null;
        try {
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.from((param.getPageNumber() - 1) * param.getPageSize());
            searchSourceBuilder.size(param.getPageSize());
            searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
            searchSourceBuilder.fetchSource(new String[]{"message", "@timestamp", "host", "type"}, new String[]{});
            Integer userId = iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId();
            MwCommonAssetsDto mwCommonAssetsDto = MwCommonAssetsDto.builder().userId(userId).monitorMode(11).build();
            List<String> hostIps = mwAssetsManager.getLogHostList(mwCommonAssetsDto);
            BoolQueryBuilder queryBuilder2 = QueryBuilders.boolQuery();
//            queryBuilder2.must(QueryBuilders.rangeQuery("@timestamp").from(MWUtils.getUtcTime(param.getQueryDate()[0]))
//                    .to(MWUtils.getUtcTime(param.getQueryDate()[1])));

            for (String hostIp : hostIps) {
                queryBuilder2 = queryBuilder2.should(QueryBuilders.termQuery("host.name", hostIp));
                queryBuilder2 = queryBuilder2.should(QueryBuilders.termQuery("fields.host", hostIp));
            }
            BoolQueryBuilder queryBuilder = null;
            if (null != param.getQueryDate()) {
                queryBuilder = QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery("@timestamp").from(MWUtils.getUtcTime(param.getQueryDate()[0]))
                                .to(MWUtils.getUtcTime(param.getQueryDate()[1])))
                        .must(queryBuilder2);
                searchSourceBuilder.query(queryBuilder);
            }

            if (null != param.getMessage() && StringUtils.isNotEmpty(param.getMessage())) {
                String message = QueryParser.escape(param.getMessage());
                if(queryBuilder != null){
                    queryBuilder = QueryBuilders.boolQuery()
                            .must(queryBuilder)
                            .must(QueryBuilders.queryStringQuery(message).field("message"))
                            .must(queryBuilder2);//设置字段模糊查询
                    searchSourceBuilder.query(queryBuilder);
                }else{
                    queryBuilder = QueryBuilders.boolQuery()
                            .must(QueryBuilders.queryStringQuery(message).field("message"))
                            .must(queryBuilder2);//设置字段模糊查询
                    searchSourceBuilder.query(queryBuilder);
                }

            }
            // add by gengjb   将类型过滤注释，目前类型是由数据库资产表进行翻译，类型直接匹配就行
            if (null != param.getType() && StringUtils.isNotEmpty(param.getType())) {
                //查询类型所对应的hostname
                List<String> asstsHostName = eslogDao.getAsstsHostName(param.getType());
                BoolQueryBuilder builder = new BoolQueryBuilder();
                if(asstsHostName != null && asstsHostName.size() > 0){
                    for (String hostName : asstsHostName) {
                        builder.should(QueryBuilders.fuzzyQuery("host.name",hostName));
                    }
                    if(queryBuilder != null){
                        queryBuilder = QueryBuilders.boolQuery()
                                .must(queryBuilder)
                                .must(builder)
                                .must(queryBuilder2);//设置字段模糊查询
                        searchSourceBuilder.query(queryBuilder);
                    }else{
                        queryBuilder = QueryBuilders.boolQuery()
                                .must(builder)
                                .must(queryBuilder2);//设置字段模糊查询
                        searchSourceBuilder.query(queryBuilder);
                    }
                }else{
                    if(queryBuilder != null){
                        queryBuilder = QueryBuilders.boolQuery()
                                .must(queryBuilder)
                                .must(QueryBuilders.matchQuery("type", param.getType()).fuzziness(Fuzziness.AUTO))
                                .must(queryBuilder2);//设置字段模糊查询
                        searchSourceBuilder.query(queryBuilder);
                    }else{
                        queryBuilder = QueryBuilders.boolQuery()
                                .must(QueryBuilders.matchQuery("type", param.getType()).fuzziness(Fuzziness.AUTO))
                                .must(queryBuilder2);//设置字段模糊查询
                        searchSourceBuilder.query(queryBuilder);
                    }
                }
            }

            if (null != param.getHost() && StringUtils.isNotEmpty(param.getHost())) {
                if(queryBuilder != null){
                    queryBuilder = QueryBuilders.boolQuery()
                            .must(queryBuilder)
                            .must(QueryBuilders.boolQuery()
                                    .should(QueryBuilders.fuzzyQuery("host.name", param.getHost()))
                                    .should(QueryBuilders.fuzzyQuery("fields.host", param.getHost())))
                            .must(queryBuilder2);//设置字段模糊查询
                    searchSourceBuilder.query(queryBuilder);
                }else{
                    queryBuilder = QueryBuilders.boolQuery()
                            .must(QueryBuilders.boolQuery()
                                    .should(QueryBuilders.fuzzyQuery("host.name", param.getHost()))
                                    .should(QueryBuilders.fuzzyQuery("fields.host", param.getHost())))
                            .must(queryBuilder2);//设置字段模糊查询
                    searchSourceBuilder.query(queryBuilder);
                }

            }
            if (null != param.getLogSource() && StringUtils.isNotEmpty(param.getLogSource())) {
                BoolQueryBuilder queryBuilder1;
                if (queryBuilder != null) {
                    queryBuilder1 = QueryBuilders.boolQuery()
                            .must(QueryBuilders.boolQuery().
                                    should(QueryBuilders.termQuery("host.name", param.getLogSource()))
                                    .should(QueryBuilders.termQuery("fields.host", param.getLogSource())))
                            .must(queryBuilder)
                    //.must(queryBuilder2)
                    ;
                } else {
                    queryBuilder1 = QueryBuilders.boolQuery()
                            .must(QueryBuilders.boolQuery().
                                    should(QueryBuilders.termQuery("host.name", param.getLogSource()))
                                    .should(QueryBuilders.termQuery("fields.host", param.getLogSource())))
                    //.must(queryBuilder2)
                    ;
                }
                searchSourceBuilder.query(queryBuilder1);
            } else {
                searchSourceBuilder.query(queryBuilder);
            }
            SearchRequest searchRequest = new SearchRequest();
            if(StringUtils.isNotBlank(param.getMessage())){
                //如果message不为空的华，设置索引，如果与排序结合，条件会失效
                String todayDate = MWUtils.getSolarData(0, 0, 0, "yyyy.MM.dd", 0);
                String index = "10.18.5.19_filebeat_"+todayDate+".log";
                searchRequest.indices(index);
            }else {
                searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.DESC));
            }
            searchRequest.source(searchSourceBuilder);


//            SearchRequest searchRequest2 = new SearchRequest();

            try {
                SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
                List<Map<String, Object>> list = new ArrayList<>();
                String typeName = param.getType();
                for (SearchHit searchHit : search.getHits().getHits()) {
                    errorSearchHit = searchHit;
                    Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                    sourceAsMap.put("id", searchHit.getId());
                    sourceAsMap.put("index", searchHit.getIndex());
                    String hostIp = sourceAsMap.get("host").toString();
                    Object fieldHost = sourceAsMap.get("host");
                    if (null != fieldHost && fieldHost instanceof Map) {
                        Map<String, Object> jsonObject = (Map<String, Object>) fieldHost;
                        sourceAsMap.put("host", jsonObject.get("name"));
                        hostIp = jsonObject.get("name").toString();
                    }
                    String type = eslogDao.getAssetsType(hostIp);
                    if(StringUtils.isNotBlank(typeName)){
                        if(StringUtils.isNotBlank(type) && (type.contains(typeName) || typeName.contains(type))){
                            sourceAsMap.put("assetsType", type);
                            list.add(sourceAsMap);
                        }
                    }else{
                        sourceAsMap.put("assetsType", type);
                        list.add(sourceAsMap);
                    }

                }
                long count = search.getHits().getTotalHits().value;
                PageInfo pageInfo = new PageInfo<>(list);
                pageInfo.setList(list);
                pageInfo.setTotal(count);
                // restHighLevelClient.close();
                return Reply.ok(pageInfo);
            } catch (ElasticsearchException e) {//索引不存在
                log.error("getMessageList_ElasticsearchException{}", e);
                PageInfo pageInfo = new PageInfo<>();
                return Reply.ok(pageInfo);
            }

        } catch (Exception e) {
            if(null != errorSearchHit){
                log.info(errorSearchHit.toString());
            }
            log.error("fail to getLogList with d={}, cause:{}", param, e);
            return Reply.fail(ErrorConstant.LOGGER_MESSAGE_BROWSE_CODE_310004, ErrorConstant.LOGGER_MESSAGE_BROWSE_MSG_310004);
        }
    }
}
