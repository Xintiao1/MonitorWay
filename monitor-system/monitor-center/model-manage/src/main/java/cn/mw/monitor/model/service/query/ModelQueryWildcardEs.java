package cn.mw.monitor.model.service.query;

import cn.mw.monitor.service.model.param.QueryModelExist;
import cn.mw.monitor.service.model.param.QueryModelWildcard;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

public class ModelQueryWildcardEs implements ModelQuery{
    private QueryModelWildcard queryModelWildcard;

    public ModelQueryWildcardEs(QueryModelWildcard queryModelWildcard){
        this.queryModelWildcard = queryModelWildcard;
    }

    @Override
    public QueryBuilder genQuery() {
        return QueryBuilders.wildcardQuery(queryModelWildcard.getItemName() + ".keyword", "*" + queryModelWildcard.getValue() + "*");
    }
}
