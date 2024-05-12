package cn.mw.monitor.screen.service.modelImpl;

import cn.mw.monitor.screen.service.WebSocket;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.screen.dao.MWLagerScreenDao;
import cn.mw.monitor.screen.dto.ModelContentDto;
import cn.mw.monitor.screen.service.MWModelManage;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.concurrent.*;


/**
 * @author xhy
 * @date 2020/12/19 14:48
 */
@Data
public abstract class BaseModel {
    @Autowired
    public WebSocket webSocket;

    @Resource
    public MWLagerScreenDao dao;

    @Autowired
    public MWModelManage mwModelManage;

    @Autowired
    public RedisTemplate<String, String> redisTemplate;

    @Autowired
    public ILoginCacheInfo iLoginCacheInfo;

    public static ConcurrentHashMap<String, Thread> threadHashMap = new ConcurrentHashMap<>();
//    public static ThreadPoolExecutor executorService = new ThreadPoolExecutor(100, 150, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
//
//    static {
//        executorService.allowCoreThreadTimeOut(true);
//    }


    //创建ThreadPoolTaskScheduler线程池
    @Autowired
    @Lazy
    ThreadPoolExecutor executorService;

//    @Autowired
//    @Lazy
//    ExecutorService executorService;

    @Bean
    public ExecutorService executorService() {
        ThreadPoolExecutor executorService = new ThreadPoolExecutor(100, 150, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        executorService.allowCoreThreadTimeOut(true);
//        ExecutorService executorService = Executors.newCachedThreadPool();
        //设置线程池大小，在线程池中执行的方法都是异步，但是当同一时间需要执行的方法大于线程池的大小时，就会出现等待连接
        //相当于同步的情况，需要注意
        return executorService;
    }

    public String genRedisKey(String methodName, String objectName, Integer uid) {
        StringBuffer sb = new StringBuffer();
        sb.append(methodName).append(":").append(objectName)
                .append("_").append(uid);
        return sb.toString();
    }


    public abstract void process(ModelContentDto model);

}
