package cn.mw.monitor.script.util;

import java.util.concurrent.*;

/**
 * @author lumingming
 * @createTime 2023919 15:00
 * @description 超时队列
 */
public class MethodTimeoutQueue {
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public <T> T executeWithTimeout(Callable<T> task, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        Future<T> future = executorService.submit(task);
        try {
            return future.get(timeout, unit);
        } catch (TimeoutException e) {
            // 任务超时处理逻辑
            future.cancel(true); // 取消任务
            throw e;
        }
    }

    public void shutdown() {
        executorService.shutdown();
    }


    public static void sleepLongTime() throws InterruptedException {
        Thread.sleep(5000);
    }



    public static void main(String[] args) {
        MethodTimeoutQueue queue = new MethodTimeoutQueue();
        try {
            String result = queue.executeWithTimeout(() -> {
                // 在这里执行需要超时控制的方法
                // 可能是某个耗时的操作
                // 模拟耗时操作
                sleepLongTime();
                return "Task completed!";
            }, 4, TimeUnit.SECONDS);

            System.out.println("Result: " + result);
        } catch (TimeoutException | InterruptedException | ExecutionException e) {
            // 处理超时或其他异常
            System.err.println("Task execution failed: " + e.getMessage());
        } finally {
            queue.shutdown();
        }
    }
}
