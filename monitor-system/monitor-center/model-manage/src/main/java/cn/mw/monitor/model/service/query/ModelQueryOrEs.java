package cn.mw.monitor.model.service.query;

import cn.mw.monitor.service.model.util.MwModelUtils;
import cn.mw.monitor.service.model.param.QueryModelOr;
import cn.mw.monitor.service.model.param.QueryModelParam;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

@Slf4j
public class ModelQueryOrEs implements ModelQuery{
    private QueryModelOr queryModelOr;

    public ModelQueryOrEs(QueryModelOr queryModelOr){
        this.queryModelOr = queryModelOr;
    }

    @Override
    public BoolQueryBuilder genQuery() {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        Class dataType = queryModelOr.getDataType();
        if(!ModelQueryUtil.isModelQueryType(dataType)) {
            for (Object data : queryModelOr.getDataList()) {
                QueryBuilder queryBuilder1 = MwModelUtils.tranformEsQuery(queryModelOr.getItemName(), data);
                queryBuilder.should(queryBuilder1);
            }
        }else{
            for(Object data : queryModelOr.getDataList()){
                QueryModelParam queryModelParam = (QueryModelParam) data;
                ModelQuery modelQuery = ModelQueryFactory.genModelQuery(queryModelParam);
                if(null != modelQuery){
                    QueryBuilder queryBuilder1 = modelQuery.genQuery();
                    queryBuilder.should(queryBuilder1);
                }else{
                    log.error("genQuery {}" ,queryModelParam.toString());
                }

            }

        }
        return queryBuilder;
    }
}
