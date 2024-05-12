package cn.mw.monitor.screen.service.modelImpl;

import cn.mwpaas.common.utils.StringUtils;
import cn.mw.monitor.screen.service.WebSocket;
import cn.mw.monitor.screen.dto.ModelContentDto;
import cn.mw.monitor.screen.model.FilterAssetsParam;
import cn.mw.monitor.state.DataType;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author xhy
 * @date 2021/1/11 10:38
 * 6  告警统计
 */
@Component
public class AlertCountAcknowModel extends BaseModel {
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
                            logger.info("{getAlertCount_host_acknowledged}", modelDataId);
                            String redislist = redisTemplate.opsForValue().get(genRedisKey("getAlertCount_host_acknowledged", modelDataId, userId));
                            JSONObject object = new JSONObject();
                            if (null != redislist && StringUtils.isNotEmpty(redislist)) {
                                object = JSONObject.parseObject(redislist);
                            } else {
                                FilterAssetsParam filterAssetsParam = FilterAssetsParam.builder().modelId(model.getModelId()).modelDataId(modelDataId).userId(userId).type(DataType.SCREEN.getName()).build();
                                object = mwModelManage.getAlertCount(filterAssetsParam);
                            }
                            webSocket.sendObjMessage(userId,modelDataId, object);
                            try {
                                Thread.sleep(1000 * model.getTimeLag());
                            } catch (InterruptedException e) {
                                logger.info("线程执行InterruptedException{}", e);
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
