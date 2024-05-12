package cn.mw.monitor.model.service.query;

import cn.mw.monitor.service.model.param.QueryModelExist;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

public class ModelQueryExistEs implements ModelQuery{
    private QueryModelExist queryModelExist;

    public ModelQueryExistEs(QueryModelExist queryModelExist){
        this.queryModelExist = queryModelExist;
    }

    @Override
    public QueryBuilder genQuery() {
        return QueryBuilders.existsQuery(queryModelExist.getItemName());
    }
}
