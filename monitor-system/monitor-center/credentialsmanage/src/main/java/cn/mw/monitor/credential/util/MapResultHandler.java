package cn.mw.monitor.credential.util;

import org.apache.ibatis.session.ResultContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zy.quaee on 2021/4/22 17:04.
 **/
@SuppressWarnings("all")
public class MapResultHandler<K,V> implements org.apache.ibatis.session.ResultHandler<Map<K,V>> {

    private final Map<K,V> mappedResults = new HashMap<>();
    @Override
    public void handleResult(ResultContext<? extends Map<K, V>> resultContext) {
        Map map = (Map) resultContext.getResultObject();
        mappedResults.put((K)map.get("key"), (V)map.get("value"));
    }

    public Map<K,V> getMappedResults() {
        return mappedResults;
    }
}
