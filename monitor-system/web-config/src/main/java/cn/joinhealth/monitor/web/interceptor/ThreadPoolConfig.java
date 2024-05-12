//package cn.joinhealth.monitor.web.interceptor;
//
//import lombok.Data;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.scheduling.annotation.EnableAsync;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
//
//import java.util.concurrent.ThreadPoolExecutor;
//
///**
// * @author xhy
// * @date 2020/5/5 20:41
// */
//@EnableAsync
//@Configuration
//public class ThreadPoolConfig {
//    @Bean(name = "webThreadPool")
//    public ThreadPoolTaskExecutor webThreadPoolTask() {
//        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//        ThreadPoolProperties properties = this.ThreadPoolProperties();
//
//        executor.setCorePoolSize(properties.getCorePoolSize());
//        executor.setMaxPoolSize(properties.getMaxPoolSize());
//        executor.setQueueCapacity(properties.getQueueCapacity());
//        executor.setKeepAliveSeconds(properties.getKeepAliveSeconds());
//        executor.setThreadNamePrefix(properties.getThreadName());
//        switch (properties.getRejectedExecutionHandler()) {
//            case "abortPolicy":
//                executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
//                break;
//            case "callerRunsPolicy":
//                executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
//                break;
//            case "discardOldestPolicy":
//                executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
//                break;
//            case "discardPolicy":
//                executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
//                break;
//            default:
//                executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
//                break;
//        }
//        executor.initialize();
//        return executor;
//    }
//
//    @Bean
//    @ConfigurationProperties(prefix = "threadpool.web")
//    public ThreadPoolProperties ThreadPoolProperties() {
//        return new ThreadPoolProperties();
//    }
//
//
//    @Data
//    @Configuration
//    public static class ThreadPoolProperties {
//
//        /**
//         * 线程前缀名
//         */
//        private String threadName;
//        /**
//         * 核心线程池大小
//         */
//        private int corePoolSize;
//        /**
//         * 最大线程数
//         */
//        private int maxPoolSize;
//        /**
//         * 队列大小
//         */
//        private int queueCapacity;
//        /**
//         * 线程池维护空闲线程存在时间
//         */
//        private int keepAliveSeconds;
//        /**
//         * 拒绝策略
//         */
//        private String rejectedExecutionHandler;
//
//    }
//}
