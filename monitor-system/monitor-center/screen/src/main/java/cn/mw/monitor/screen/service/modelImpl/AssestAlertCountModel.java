package cn.mw.monitor.screen.service.modelImpl;

import cn.mw.monitor.service.assets.param.QueryTangAssetsParam;
import cn.mwpaas.common.utils.StringUtils;
import cn.mw.monitor.screen.service.WebSocket;
import cn.mw.monitor.screen.dto.AlarmTypeCount;
import cn.mw.monitor.screen.dto.ModelContentDto;
import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author xhy
 * @date 2021/1/11 10:34
 * 18 资产概况
 */
@Slf4j
@Component
public class AssestAlertCountModel extends BaseModel {
    private static final Logger logger = LoggerFactory.getLogger("cn/mw/monitor/screen/AssestAlertCountModel");

    @Override
    public void process(ModelContentDto model) {
//        ThreadPoolExecutor executorService = new ThreadPoolExecutor(9, 20, 3, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy());
//        executorService.allowCoreThreadTimeOut(true);
        String modelDataId = model.getModelDataId();
        Integer userId = model.getUserId();
        Integer modelId = model.getModelId();

        switch (modelId){
            case 18:
                processAssetAlert(model,modelDataId,userId);
                break;
            case 30:
               processAssetByType(model,modelDataId,userId);
                break;
            case 29:
                processAssetByOrgs(model,modelDataId,userId);
                break;
        }

    }

    private void processAssetByOrgs(ModelContentDto model, String modelDataId, Integer userId) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                threadHashMap.put(userId + userId + modelDataId, Thread.currentThread());
                while (null != WebSocket.sessionPool.get(userId +modelDataId)) {
                    try {
                        List<Map<String, List<Integer>>> list = mwModelManage.getAsseOrg(QueryTangAssetsParam.builder().isSelectLabel(false).userId(userId).build());
                        log.info("ScreenprocessAssetByOrgs:"+list);
                        webSocket.sendObjMessage(userId,modelDataId, list);
                        Thread.sleep(1000 * model.getTimeLag());
                    } catch (InterruptedException e) {
                        log.error("线程执行processAssetByOrgs{}", e);
                    }
                }
            }
        };
        executorService.execute(runnable);
    }

    private void processAssetByType(ModelContentDto model, String modelDataId, Integer userId) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                threadHashMap.put(userId + userId + modelDataId, Thread.currentThread());
                while (null != WebSocket.sessionPool.get(userId +modelDataId)) {
                    try {
                    List<Map<String, List<Integer>>> list = mwModelManage.getAsseTable(QueryTangAssetsParam.builder().isSelectLabel(false).userId(userId).build());
                    log.info("ScreenprocessAssetByType:"+list);
                    webSocket.sendObjMessage(userId,modelDataId, list);
                    Thread.sleep(1000 * model.getTimeLag());
                    } catch (InterruptedException e) {
                        log.error("线程执行processAssetByType{}", e);
                    }
                }
            }
        };
        executorService.execute(runnable);
    }

    private void processAssetAlert(ModelContentDto model, String modelDataId, Integer userId) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                threadHashMap.put(userId + userId + modelDataId, Thread.currentThread());
                while (null != WebSocket.sessionPool.get(userId +modelDataId)) {
                    logger.info("{saveAlertCount_host_count}", modelDataId);
                    try {
                        String redislist = redisTemplate.opsForValue().get(genRedisKey("saveAlertCount_host_count", modelDataId, userId));
                        List<AlarmTypeCount> list = new ArrayList<>();
                        if (null != redislist && StringUtils.isNotEmpty(redislist)) {
                            list = JSONArray.parseArray(redislist, AlarmTypeCount.class);
                        } else {
                            list = mwModelManage.hostgroupListGetByName(model.getModelContent(), userId);
                        }
                        logger.info("AssestAlertCountModel1 "+list);
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
    }
}
