package cn.mw.monitor.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/*
 * 单线程,且队列容量只有1
 * 便于在服务中,处理主要任务后直接返回,次要任务则加入后台逐个处理
 */
@Component
@Slf4j
public class SingleThreadExeManage {
    private Thread monitorThread;
    private long defualtKeepaliveInterval = 5000;
    private Map<String ,ExecutorService> executorMap = new HashMap<>();
    private Map<String ,ServiceStatus> statusMap = new HashMap<>();

    public synchronized void addSingQueueTask(String service ,Runnable task){
        addTask(service ,task ,1 ,defualtKeepaliveInterval);
    }

    public synchronized void addSingQueueTask(String service ,Runnable task ,long keepaliveInterval){
        addTask(service ,task ,1 ,keepaliveInterval);
    }

    public synchronized void addNoLimitQueueTask(String service ,Runnable task){
        addTask(service ,task ,-1 ,defualtKeepaliveInterval);
    }

    public synchronized void addNoLimitQueueTask(String service ,Runnable task ,long keepaliveInterval){
        addTask(service ,task ,-1 ,keepaliveInterval);
    }

    private void addTask(String service ,Runnable task ,int queueLimit ,long keepaliveInterval){
        log.info("SingleThreadExeManage addTask {}" ,service);
        //设置服务当前访问时间
        ServiceStatus status = statusMap.get(service);
        if(null == status){
            status = new ServiceStatus(keepaliveInterval);
        }
        status.visit();

        ExecutorService executor = executorMap.get(service);
        if(null == executor || executor.isShutdown()){
            if(queueLimit < 0){
                executor = new ThreadPoolExecutor(1, 1,
                        0L, TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<Runnable>());
            }else {
                executor = new ThreadPoolExecutor(1, 1,
                        0L, TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<Runnable>(queueLimit));
            }
        }
        executor.submit(task);

        if(null == monitorThread || !monitorThread.isAlive()){
            monitorThread = new Thread(new MonitorTask(executorMap ,statusMap));
            monitorThread.start();
        }

    }
}
