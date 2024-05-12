package cn.mw.monitor.screen.service.modelImpl;

import cn.mw.monitor.screen.service.WebSocket;
import cn.mw.monitor.screen.dto.AlertTodayHistory;
import cn.mw.monitor.screen.dto.ModelContentDto;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author xhy
 * @date 2021/1/11 11:40
 * 19 今日告警趋势
 */
@Slf4j
@Component
public class AlertTodayHistoryModel extends BaseModel {
    private static final Logger logger = LoggerFactory.getLogger("cn/mw/monitor/screen/AssestAlertCountModel");

    @Override
    public void process(ModelContentDto model) {
//        ThreadPoolExecutor executorService = new ThreadPoolExecutor(9, 20, 3, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy());
//         executorService.allowCoreThreadTimeOut(true);
        String modelDataId = model.getModelDataId();
        Integer userId = model.getUserId();

        try {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    threadHashMap.put(userId + modelDataId, Thread.currentThread());
                    while (null != WebSocket.sessionPool.get(userId +modelDataId)) {
                        logger.info("{getAlertTodayHistory}", modelDataId);
                        try {
                            String redisList = redisTemplate.opsForValue().get(genRedisKey("getAlertTodayHistory", modelDataId, userId));
                            AlertTodayHistory alertTodayHistory = new AlertTodayHistory();
                            if (null != redisList && StringUtil.isNotEmpty(redisList)) {
                                alertTodayHistory = JSONArray.parseObject(redisList, AlertTodayHistory.class);
                            } else {
                                alertTodayHistory = mwModelManage.getAlertTodayHistory(userId, model.getModelId());
                            }
                            webSocket.sendObjMessage(userId,modelDataId, alertTodayHistory);

                            Thread.sleep(1000 * model.getTimeLag());
                        } catch (InterruptedException e) {
                            log.error("线程执行InterruptedException{}", e);
                            break;
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
