package cn.mw.monitor.screen.service.modelImpl;

import cn.mw.monitor.screen.service.WebSocket;
import cn.mw.monitor.screen.dto.ModelContentDto;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author xhy
 * @date 2021/1/11 14:22
 * 24被攻击IP地址次数排行榜
 * 25攻击IP地址次数排行榜
 */
@Component
public class OccurCountIpModel extends BaseModel {
    private static final Logger logger = LoggerFactory.getLogger("cn/mw/monitor/screen/OccurCountSourceIpModel");


    @Override
    public void process(ModelContentDto model) {
//         ThreadPoolExecutor executorService = new ThreadPoolExecutor(9, 20, 3, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy());
//        executorService.allowCoreThreadTimeOut(true);
        String modelDataId = model.getModelDataId();
        Integer userId = model.getUserId();

        int timeLag = dao.getBulkDataTimeCount(modelDataId, userId) == 0 ? 600 : dao.getBulkDataTime(modelDataId, userId);
        try {
            Runnable runnable = new Runnable() {

                @Override
                public void run() {
                        threadHashMap.put(userId + modelDataId, Thread.currentThread());
                        while (null != WebSocket.sessionPool.get(userId +modelDataId)) {
                            logger.info("{getOccurCount_sourceIp}", modelDataId);
                            try {
                                String redisList = redisTemplate.opsForValue().get(genRedisKey("getOccurCount_" + model.getModelContent(), modelDataId, userId));
                                List<Map> list = new ArrayList<>();
                                if (null != redisList && StringUtil.isNotEmpty(redisList)) {
                                    list = JSONArray.parseArray(redisList, Map.class);
                                } else {
                                    list = mwModelManage.getOccurCount(model.getModelContent());
                                }
                                webSocket.sendObjMessage(userId,modelDataId, list);

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
