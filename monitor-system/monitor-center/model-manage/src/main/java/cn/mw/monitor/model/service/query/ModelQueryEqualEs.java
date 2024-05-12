package cn.mw.monitor.model.service.query;

import cn.mw.monitor.service.model.param.QueryModelEq;
import cn.mw.monitor.service.model.param.QueryModelExist;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

public class ModelQueryEqualEs implements ModelQuery{
    private QueryModelEq queryModelEq;

    public ModelQueryEqualEs(QueryModelEq queryModelEq){
        this.queryModelEq = queryModelEq;
    }

    @Override
    public QueryBuilder genQuery() {
        Object value = queryModelEq.getValue();
        if(value instanceof Integer){
            return QueryBuilders.termQuery(queryModelEq.getItemName() ,Integer.parseInt(value.toString()));
        }else if (value instanceof String){
            return QueryBuilders.termQuery(queryModelEq.getItemName() ,value.toString());
        }
        return null;
    }
}
