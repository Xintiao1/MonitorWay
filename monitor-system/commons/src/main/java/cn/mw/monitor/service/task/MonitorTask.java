package cn.mw.monitor.service.task;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
public class MonitorTask implements Runnable {

    private long sleepTime = 3000;
    private Map<String ,ExecutorService> executorMap = new HashMap<>();
    private Map<String ,ServiceStatus> statusMap = new HashMap<>();

    public MonitorTask(Map<String , ExecutorService> executorMap ,Map<String ,ServiceStatus> statusMap){
        this.executorMap = executorMap;
        this.statusMap = statusMap;
    }

    @Override
    public void run() {
        while(true){
            try {
                Thread.sleep(sleepTime);
            }catch (Exception e){
                log.error("MonitorTask" ,e);
            }

            Iterator<Map.Entry<String ,ServiceStatus>> iterator = statusMap.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry<String ,ServiceStatus> next = iterator.next();
                ServiceStatus serviceStatus = next.getValue();
                if(serviceStatus.isExceedInteval()){
                    String service = next.getKey();
                    ThreadPoolExecutor executorService = (ThreadPoolExecutor)executorMap.get(service);
                    if(null != executorService && executorService.getActiveCount() == 0){
                        executorService.shutdownNow();
                        executorMap.remove(service);
                        statusMap.remove(service);
                        log.info("remove service {}" ,service);
                    }
                }
            }

            //当没有任务时,退出监控
            if(statusMap.size() <= 0){
                log.info("MonitorTask exit");
                break;
            }
        }
    }
}
