package cn.mw.monitor.es.action;

import cn.mwpaas.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class EsDataOperation {
    private RestHighLevelClient client;
    private final RequestOptions options = RequestOptions.DEFAULT;

    public EsDataOperation(RestHighLevelClient client){
        this.client = client;
    }
    /**
     * 写入数据
     */
    public boolean insert(String indexName, Map<String, Object> dataMap){
        try {
            BulkRequest request = new BulkRequest();
            request.add(new IndexRequest(indexName).id(dataMap.remove("id").toString())
                    .opType("create").source(dataMap, XContentType.JSON));
            BulkResponse response = this.client.bulk(request, options);
            if(response.hasFailures()){
                log.error("insert" ,response.buildFailureMessage());
                return false;
            }
            return true;
        } catch (Exception e){
            log.error("insert" ,e);
        }
        return false;
    }

    /**
     * 批量写入数据
     */
    public boolean batchInsert(String indexName, List<Map<String, Object>> dataList){
        try {
            BulkRequest request = new BulkRequest();
            for (Map<String, Object> dataMap:dataList){
                request.add(new IndexRequest(indexName).id(dataMap.remove("id").toString())
                        .opType("create").source(dataMap,XContentType.JSON));
            }
            BulkResponse response = this.client.bulk(request, options);
            if(response.hasFailures()){
                log.error("insert" ,response.buildFailureMessage());
                return false;
            }
            return true ;
        } catch (Exception e){
            log.error("batchInsert" ,e);
        }
        return false;
    }

    public boolean batchUpdateJsonData(List<UpdateRequest> updateRequests){
        try {
            BulkRequest request = new BulkRequest();
            for (UpdateRequest data : updateRequests){
                request.add(data);
            }
            BulkResponse response = this.client.bulk(request, options);
            if(response.hasFailures()){
                log.error("update" ,response.buildFailureMessage());
                return false;
            }
            return true ;
        } catch (Exception e){
            log.error("batchUpdate" ,e);
        }
        return false;
    }

    public boolean batchUpdateJsonData(String indexName, List<EsUpdateData> updateDataList){
        List<UpdateRequest> updateRequests = new ArrayList<>();
        for(EsUpdateData esUpdateData : updateDataList){
            String type = StringUtils.isEmpty(esUpdateData.getType())?"_doc":esUpdateData.getType();
            UpdateRequest updateRequest = new UpdateRequest(indexName, type, esUpdateData.getId());
            updateRequest.doc(XContentType.JSON, esUpdateData, esUpdateData.getValue());
            updateRequests.add(updateRequest);
        }
        return batchUpdateJsonData(updateRequests);
    }

    public boolean batchInsertJsonData(String indexName, List<String> jsonData){
        try {
            BulkRequest request = new BulkRequest();
            for (String data : jsonData){
                request.add(new IndexRequest(indexName)
                        .opType("create").source(data,XContentType.JSON));
            }
            BulkResponse response = this.client.bulk(request, options);
            if(response.hasFailures()){
                log.error("insert" ,response.buildFailureMessage());
                return false;
            }
            return true ;
        } catch (Exception e){
            log.error("batchInsert" ,e);
        }
        return false;
    }

    /**
     * 更新数据，可以直接修改索引结构
     */
    public boolean update(String indexName, Map<String, Object> dataMap){
        try {
            UpdateRequest updateRequest = new UpdateRequest(indexName, dataMap.remove("id").toString());
            updateRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
            updateRequest.doc(dataMap) ;
            UpdateResponse response = this.client.update(updateRequest, options);
            log.info(response.status().toString());
            return true;
        } catch (Exception e){
            log.error("update" ,e);
        }
        return Boolean.FALSE;
    }

    /**
     * 删除数据
     */
    public boolean delete(String indexName, String id){
        try {
            DeleteRequest deleteRequest = new DeleteRequest(indexName, id);
            DeleteResponse response = this.client.delete(deleteRequest, options);
            log.info(response.status().toString());
            return true;
        } catch (Exception e){
            log.error("delete" ,e);
        }
        return false;
    }

    public boolean deleteByQuery(String indexName, QueryBuilder queryBuilder){
        try {
            DeleteByQueryRequest deleteRequest = new DeleteByQueryRequest(indexName);
            log.info("deleteByQuery {}" ,queryBuilder.toString());
            deleteRequest.setQuery(queryBuilder);
            BulkByScrollResponse response = this.client.deleteByQuery(deleteRequest ,options);
            log.info(response.getBulkFailures().toString());
            return true;
        } catch (Exception e){
            log.error("delete" ,e);
        }
        return false;
    }

    public boolean deleteFromAliasByQuery(String alias, QueryBuilder queryBuilder){
        try {
            //根据别名查询相关索引
            EsIndexOperation esIndexOperation = new EsIndexOperation(this.client);
            Set<String> indices = esIndexOperation.getAllIndices(alias);
            String[] arrayList = indices.toArray(new String[indices.size()]);

            //删除指定索引的数据
            DeleteByQueryRequest deleteRequest = new DeleteByQueryRequest(arrayList);
            log.info("deleteFromAliasByQuery {}" ,queryBuilder.toString());
            deleteRequest.setQuery(queryBuilder);
            BulkByScrollResponse response = this.client.deleteByQuery(deleteRequest ,options);

            long esTookTime = response.getTook().nanos();
            long deleted = response.getDeleted();
            log.info("deleteFromAliasByQuery take {},delete num {}" ,esTookTime ,deleted);

            return true;
        } catch (Exception e){
            log.error("delete" ,e);
        }
        return false;
    }
}
