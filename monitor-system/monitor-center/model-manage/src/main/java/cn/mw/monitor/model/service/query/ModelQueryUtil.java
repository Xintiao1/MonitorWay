package cn.mw.monitor.model.service.query;

import cn.mw.monitor.service.model.param.QueryModelParam;

public class ModelQueryUtil {
    public static boolean isModelQueryType(Class clazz){
        return QueryModelParam.class.isAssignableFrom(clazz);
    }
}
