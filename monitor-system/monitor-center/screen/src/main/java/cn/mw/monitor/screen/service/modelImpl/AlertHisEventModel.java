package cn.mw.monitor.screen.service.modelImpl;

import cn.mw.monitor.screen.service.WebSocket;
import cn.mw.monitor.screen.dto.HistEventDto;
import cn.mw.monitor.screen.dto.ModelContentDto;
import cn.mw.monitor.screen.model.FilterAssetsParam;
import cn.mw.monitor.state.DataType;
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
 * @date 2021/1/11 11:36
 * 12 过去N条历史事件
 */
@Slf4j
@Component
public class AlertHisEventModel extends BaseModel {
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
                    threadHashMap.put(userId + modelDataId, Thread.currentThread());

                    while (null != WebSocket.sessionPool.get(userId +modelDataId)) {
                        try {
                            logger.info("{getAlertEvent_histEvent}", modelDataId);
                            String redislist = redisTemplate.opsForValue().get(genRedisKey("getAlertEvent_histEvent", modelDataId, userId));
                            List<HistEventDto> histEventDtos = new ArrayList<>();
                            if (null != redislist && StringUtil.isNotEmpty(redislist)) {
                                histEventDtos = JSONArray.parseArray(redislist, HistEventDto.class);
                            } else {
                                FilterAssetsParam filterAssetsParam = FilterAssetsParam.builder().modelId(model.getModelId()).modelDataId(modelDataId).userId(userId).type(DataType.SCREEN.getName()).build();
                                histEventDtos = mwModelManage.getHistEvent(100, filterAssetsParam);
                            }
                            if (histEventDtos.size() > 10) {
                                histEventDtos = histEventDtos.subList(0, 10);
                            }
                            webSocket.sendObjMessage(userId,modelDataId, histEventDtos);

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
