package cn.mw.monitor.es.action;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.cluster.metadata.AliasMetadata;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
public class EsIndexOperation {
    private RestHighLevelClient client ;
    private final RequestOptions options = RequestOptions.DEFAULT;

    public EsIndexOperation(RestHighLevelClient client){
        this.client = client;
    }

    /**
     * 判断索引是否存在
     */
    public boolean checkIndex(String index) {
        try {
            return client.indices().exists(new GetIndexRequest(index), options);
        } catch (IOException e) {
            log.error("checkIndex" ,e);
        }
        return false;
    }

    /**
     * 判断字段是否存在
     */
    public boolean checkField(String index ,String field) {
        try {
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            queryBuilder.must(QueryBuilders.existsQuery(field));
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            //设置超时时间
            searchSourceBuilder.query(queryBuilder);
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.source(searchSourceBuilder);
            searchRequest.indices(index);
            SearchResponse search = client.search(searchRequest, RequestOptions.DEFAULT);
            long count = search.getHits().getTotalHits().value;
            if (count > 0) {
                return true;
            }
        } catch (IOException e) {
            log.error("fail to checkFieldExist param{}, case by {}", index + ":" + field, e);
        }
        return false;
    }

    /**
     * 添加字段字符串类型添加不分词排序
     *
     * @param indexName
     * @param fieldName
     */
    public void createESMappingByString(String indexName, String fieldName) {
        PutMappingRequest request = new PutMappingRequest(indexName);
        request.type("_doc");
        Map properties = new HashMap();
        Map field = new HashMap();
        Map value = new HashMap();
        Map fields = new HashMap();
        Map type = new HashMap();
        value.put("type", "text");
        value.put("fields", fields);
        fields.put("keyword", type);
        type.put("type", "keyword");
        field.put(fieldName, value);
        properties.put("properties", field);
        request.source(JSONObject.toJSONString(properties), XContentType.JSON);
        try {
            client.indices().putMapping(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("fail to setESMappingByString param{}, case by {}", indexName + ":" + fieldName, e);
        }
    }

    /**
     * 创建索引
     */
    public boolean createIndex(String indexName , Map<String, Object> columnMap){
        try {
            if(!checkIndex(indexName)){
                CreateIndexRequest request = new CreateIndexRequest(indexName);
                if (columnMap != null && columnMap.size()>0) {
                    Map<String, Object> source = new HashMap<>();
                    source.put("properties", columnMap);
                    request.mapping(source);
                }
                this.client.indices().create(request, options);
                return true ;
            }
        } catch (IOException e) {
            log.error(e.toString());
        }
        return false;
    }
    /**
     * 删除索引
     */
    public boolean deleteIndex(String indexName) {
        try {
            if(checkIndex(indexName)){
                DeleteIndexRequest request = new DeleteIndexRequest(indexName);
                AcknowledgedResponse response = client.indices().delete(request, options);
                return response.isAcknowledged();
            }
        } catch (Exception e) {
            log.error(e.toString());
        }
        return false;
    }

    public Set<String> getAllIndices(String aliasName) throws Exception{
        GetAliasesRequest getAliasesRequest = new GetAliasesRequest(aliasName);
        GetAliasesResponse getAliasesResponse = client.indices().getAlias(getAliasesRequest ,RequestOptions.DEFAULT);
        Map<String, Set<AliasMetadata>> map = getAliasesResponse.getAliases();
        return map.keySet();
    }

}
