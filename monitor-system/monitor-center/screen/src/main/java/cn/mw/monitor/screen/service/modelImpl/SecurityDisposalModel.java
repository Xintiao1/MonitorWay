package cn.mw.monitor.screen.service.modelImpl;

import cn.mw.monitor.screen.service.WebSocket;
import cn.mw.monitor.screen.dto.ModelContentDto;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author xhy
 * @date 2021/1/11 15:22
 * <p>
 * 22 处理状态结果统计
 */
@Component
public class SecurityDisposalModel extends BaseModel {
    private static final Logger logger = LoggerFactory.getLogger("cn/mw/monitor/screen/SecurityAlertLevelModel");

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
                    threadHashMap.put(userId+modelDataId,Thread.currentThread());
                    while (null != WebSocket.sessionPool.get(userId +modelDataId)) {
                        logger.info("{getSecurityCountByField_disposalStatus}", modelDataId);
                        try {
                        String redisLeft = redisTemplate.opsForValue().get(genRedisKey("getSecurityCountByField_disposalStatus_left", modelDataId, userId));
                        String redisRight = redisTemplate.opsForValue().get(genRedisKey("getSecurityCountByField_disposalStatus_right", modelDataId, userId));
                        Map map = new HashMap();
                        if (null != redisLeft && StringUtil.isNotEmpty(redisLeft) && null != redisRight && StringUtil.isNotEmpty(redisRight)) {
                            Map<String, String> mapLeft = JSONArray.parseObject(redisLeft, Map.class);
                            Map<String, String> mapRight = JSONArray.parseObject(redisRight, Map.class);
                            Set<String> setLeft = mapLeft.keySet();
                            Set<String> setRight = mapRight.keySet();
                            for (String keyLeft : setLeft) {
                                map.put(keyLeft, mapLeft.get(keyLeft));
                            }
                            for (String keyRight : setRight) {
                                map.put(keyRight, mapRight.get(keyRight));
                            }
                        } else {
                            map = (Map) mwModelManage.getSecurityCountByField("disposalStatus");
                        }
                        webSocket.sendObjMessage(userId,modelDataId, map);

                            Thread.sleep(1000 * model.getTimeLag());
                        } catch (InterruptedException e) {
                            logger.error("线程执行InterruptedException{}", e);
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
