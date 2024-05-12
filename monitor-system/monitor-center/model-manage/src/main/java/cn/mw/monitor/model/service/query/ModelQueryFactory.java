package cn.mw.monitor.model.service.query;

import cn.mw.monitor.service.model.param.*;

public class ModelQueryFactory {
    public static ModelQuery genModelQuery(QueryModelParam query){
        ModelQueryType type = query.getType();
        ModelQuery modelQuery = null;
        switch (type){
            case AND:
                modelQuery = new ModelQueryAndEs((QueryModelAnd)query);
                break;
            case OR:
                modelQuery = new ModelQueryOrEs((QueryModelOr)query);
                break;
            case Equal:
                modelQuery = new ModelQueryEqualEs((QueryModelEq)query);
                break;
            case Exist:
                modelQuery = new ModelQueryExistEs((QueryModelExist) query);
                break;
            case Wildcard:
                modelQuery = new ModelQueryWildcardEs((QueryModelWildcard) query);
        }

        return modelQuery;
    }
}
