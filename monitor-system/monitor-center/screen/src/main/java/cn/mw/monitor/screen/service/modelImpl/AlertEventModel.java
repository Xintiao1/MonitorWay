package cn.mw.monitor.screen.service.modelImpl;

import cn.mw.monitor.screen.dto.ModelContentDto;
import cn.mw.monitor.screen.service.WebSocket;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author xhy
 * @date 2021/1/11 11:34
 * 11 活动告警
 */
@Slf4j
@Component
public class AlertEventModel  extends BaseModel {
    private static final Logger logger = LoggerFactory.getLogger("cn/mw/monitor/screen/AssestAlertCountModel");

    @Override
    public void process(ModelContentDto model) {
//        ThreadPoolExecutor executorService = new ThreadPoolExecutor(9, 20, 3, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy());
//        executorService.allowCoreThreadTimeOut(true);
        String modelDataId = model.getModelDataId();
        Integer userId = model.getUserId();

        try {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    synchronized (this) {
                        threadHashMap.put(userId + modelDataId, Thread.currentThread());
                        while (null != WebSocket.sessionPool.get(userId +modelDataId)) {
                            try {
                                log.info("查询大屏告警");
                                Map<String, Object> currGiveAnAlarm = mwModelManage.getCurrGiveAnAlarm(userId);
                                log.info("查询大屏告警2"+currGiveAnAlarm);
                                webSocket.sendObjMessage(userId,modelDataId, currGiveAnAlarm);
                                Thread.sleep(1000 * model.getTimeLag());
                            } catch (InterruptedException e) {
                                log.error("线程执行InterruptedException{}", e);
                            }
                        }
                    }
                }
            };
            executorService.execute(runnable);
        } catch (Exception e) {
            logger.error("SCREEN_LOG[]screen[]大屏[]查询大屏的组件数据[]{}", e);
        }
    }
}
