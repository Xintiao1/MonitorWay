package cn.mw.monitor.service.runtimeCache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class CacheManager {
    @Value("${monitor.runtimeCache.interval}")
    private int defaultInterval;

    private static Map<String ,CacheData> dataMap = new ConcurrentHashMap();

    public <T> T getCacheData(Class<T> clazz ,String key){
        CacheData cacheData = dataMap.get(key);
        try {
            if (null == cacheData) {
                cacheData = (CacheData)clazz.newInstance();
                cacheData.setFakeData(true);
                dataMap.put(key ,cacheData);
            }
        }catch (Exception e){
            log.error("getCacheData" ,e);
        }

        return clazz.cast(cacheData);
    }

    public void register(String key ,CacheData cacheData){
        dataMap.put(key ,cacheData);
    }

    public int getDefaultInterval() {
        return defaultInterval;
    }

    /*
     * 定时清理map
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void cleanCacheData(){
        long now = new Date().getTime();
        Iterator<Map.Entry<String ,CacheData>> entries = dataMap.entrySet().iterator();
        int count = 0;
        log.info("clean data num before:{}" ,dataMap.size());
        while (entries.hasNext()) {
            Map.Entry<String ,CacheData> entry = entries.next();
            CacheData value = entry.getValue();
            if(value.isRemoveAble(now)){
                entries.remove();
                count++;
            }
        }
        log.info("clean data num:{}, after:{}" ,count ,dataMap.size());
    }
}
