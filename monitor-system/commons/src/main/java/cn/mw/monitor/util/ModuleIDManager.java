package cn.mw.monitor.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
public class ModuleIDManager implements InitializingBean {
    private static final String ID_GENERATOR_LASTTIME = "ID_GENERATOR_LASTTIME";

    @Value("${monitor.datacenterId}")
    private long datacenterId;

    @Value("${monitor.machineId}")
    private long machineId;

    @Value("monitor.lastGenTime")
    private String lastGenTimeStr;

    private String key;

    @Autowired
    private RedisUtils redisUtils;

    private AtomicLong lastGenTime = new AtomicLong(-1);

    private Timer timer;

    //id生成时间更新时间间隔
    private int updateInteval = 60000;

    private static Map<IDModelType ,IDGenerator> idGeneratorMap;

    public long getID(IDModelType type){
        IDGenerator idGenerator = idGeneratorMap.get(type);
        long nextId = idGenerator.nextId();
        long now = System.currentTimeMillis();
        lastGenTime.set(now);
        return nextId;
    }

    public String getIDStr(IDModelType type){
        long id = getID(type);
        return String.valueOf(id);
    }

    public List<IDGenerator> getIDGenerators(){
        return new ArrayList<IDGenerator>(idGeneratorMap.values());
    }

    public AtomicLong getLastGenTime() {
        return lastGenTime;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        idGeneratorMap = new HashMap<>();
        //从redis获取上一次id更新时间
        this.key = ID_GENERATOR_LASTTIME + "-" + datacenterId + "-" + machineId;
        Long lastGenTime = null;
        Object value = redisUtils.get(key);
        if(value instanceof Integer){
            lastGenTime = ((Integer)value).longValue();
        }else{
            lastGenTime = (Long)value;
        }
        for(IDModelType type : IDModelType.values()){
            if(null != lastGenTime){
                idGeneratorMap.put(type ,new IDGenerator(datacenterId ,machineId ,lastGenTime));
            }else{
                idGeneratorMap.put(type ,new IDGenerator(datacenterId ,machineId ,-1L));
            }
        }
        log.info("lastGenTime {}" ,lastGenTime);
        timer = new Timer();
        IDSaveTask idSaveTask = new IDSaveTask(redisUtils ,key ,this);
        timer.scheduleAtFixedRate(idSaveTask, 60000, updateInteval);
    }
}
