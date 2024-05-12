package cn.mw.monitor.model.service.query;

import cn.mw.monitor.service.model.util.MwModelUtils;
import cn.mw.monitor.service.model.param.QueryModelAnd;
import cn.mw.monitor.service.model.param.QueryModelParam;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

@Slf4j
public class ModelQueryAndEs implements ModelQuery{
    private QueryModelAnd queryModelAnd;

    public ModelQueryAndEs(QueryModelAnd queryModelAnd){
        this.queryModelAnd = queryModelAnd;
    }

    @Override
    public BoolQueryBuilder genQuery() {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        Class dataType = queryModelAnd.getDataType();

        if(!ModelQueryUtil.isModelQueryType(dataType)){
            for(Object data : queryModelAnd.getDataList()){
                QueryBuilder queryBuilder1 = MwModelUtils.tranformEsQuery(queryModelAnd.getItemName() ,data);
                queryBuilder.must(queryBuilder1);
            }
        }else{
            for(Object data : queryModelAnd.getDataList()){
                QueryModelParam queryModelParam = (QueryModelParam) data;
                ModelQuery modelQuery = ModelQueryFactory.genModelQuery(queryModelParam);
                if(null != modelQuery){
                    QueryBuilder queryBuilder1 = modelQuery.genQuery();
                    queryBuilder.must(queryBuilder1);
                }else{
                    log.error("genQuery {}" ,queryModelParam);
                }

            }
        }

        return queryBuilder;
    }
}
