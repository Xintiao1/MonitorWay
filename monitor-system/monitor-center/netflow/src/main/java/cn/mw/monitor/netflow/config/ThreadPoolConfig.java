//package cn.mw.monitor.netflow.config;
//
//import cn.mwpaas.common.utils.UUIDUtils;
//import org.apache.http.client.utils.DateUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.task.AsyncTaskExecutor;
//import org.springframework.scheduling.annotation.EnableAsync;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
//
//import java.util.Date;
//import java.util.concurrent.Executor;
//import java.util.concurrent.ThreadPoolExecutor;
//
//@EnableAsync
//@Configuration
//public class ThreadPoolConfig {
//
//    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolConfig.class);
//
//    @Bean("topDataExecutor")
//    public Executor topDataExecutor() {
//        return getAsyncTaskExecutor("netflow-topData", 5, 5, 6000, 50);
//    }
//
//
//    private AsyncTaskExecutor getAsyncTaskExecutor(String threadNamePrefix, int corePoolSize, int maxPollSize,
//                                                   int queueCapacity, int keepAliveSeconds) {
//
//        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
//        taskExecutor.setCorePoolSize(corePoolSize);
//        taskExecutor.setMaxPoolSize(maxPollSize);
//        // 设置有界队列的容量
//        taskExecutor.setQueueCapacity(queueCapacity);
//        taskExecutor.setKeepAliveSeconds(keepAliveSeconds);
//        taskExecutor.setThreadNamePrefix(threadNamePrefix);
//        // 设置拒绝策略
//        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
//        taskExecutor.setRejectedExecutionHandler((r, t) -> {
//            try {
//                String id = DateUtils.formatDate(new Date(), "yyyyMMddHHmmss") + "-" + UUIDUtils.getUUID().substring(0, 7);
//                logger.warn("任务id: {}, 线程池: {}Executor. 已超过最大线程数，队列阻塞, 等待可用空间再继续; 目前队列数量: {}", id, threadNamePrefix, t.getQueue().size());
//                t.getQueue().put(r);
//                logger.warn("任务id: {}, 线程池: {}Executor. 阻塞已接触 ", id, threadNamePrefix);
//            } catch (InterruptedException e) {
//                logger.error("线程池 %sExecutor 阻塞异常: {} ", threadNamePrefix, e);
//            }
//        });
//
//        return taskExecutor;
//    }
//}
