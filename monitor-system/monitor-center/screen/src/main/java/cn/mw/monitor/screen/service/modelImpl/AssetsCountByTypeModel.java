package cn.mw.monitor.screen.service.modelImpl;

import cn.mw.monitor.screen.service.WebSocket;
import cn.mw.monitor.screen.dto.AlertPriorityType;
import cn.mw.monitor.screen.dto.ModelContentDto;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author xhy
 * @date 2021/1/11 11:38
 * 1-5 资产统计
 */
@Slf4j
@Component
public class AssetsCountByTypeModel extends BaseModel {
    private static final Logger logger = LoggerFactory.getLogger("cn/mw/monitor/screen/AssetsCountByTypeModel");

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
                            logger.info("{getAssetsCountByType}", modelDataId);
                            try {
                                String redisList = redisTemplate.opsForValue().get(genRedisKey("getAssetsCountByType", modelDataId, userId));
                                List<AlertPriorityType> list = new ArrayList<>();
                                if (null != redisList && StringUtil.isNotEmpty(redisList)) {
                                    list = JSONArray.parseArray(redisList, AlertPriorityType.class);
                                } else {
                                    list = mwModelManage.getAssetsCountByType(userId, model.getModelId());
                                }
                                webSocket.sendObjMessage(userId,modelDataId, list);
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
