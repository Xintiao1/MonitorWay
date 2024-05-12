package cn.mw.monitor.graph.topo;

import lombok.extern.slf4j.Slf4j;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ExcecuteThread extends Thread {
    private Queue<Runnable> taskQueue;
    private AtomicInteger taskNum;

    public ExcecuteThread(Queue<Runnable> taskQueue ,AtomicInteger taskNum){
        this.taskQueue = taskQueue;
        this.taskNum = taskNum;
    }

    @Override
    public void run() {
        int num = 1;
        while (num > 0){
            Runnable task = taskQueue.remove();
            if(null != task){
                task.run();
            }
            num = taskNum.decrementAndGet();
            log.info("execute num is {}" ,num);
        }

        log.info("execute finish");
    }
}