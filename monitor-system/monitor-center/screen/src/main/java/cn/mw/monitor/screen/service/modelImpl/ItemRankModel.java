package cn.mw.monitor.screen.service.modelImpl;

import cn.mwpaas.common.utils.StringUtils;
import cn.mw.monitor.screen.service.WebSocket;
import cn.mw.monitor.screen.dto.ItemRank;
import cn.mw.monitor.screen.dto.ModelContentDto;
import cn.mw.monitor.screen.model.FilterAssetsParam;
import cn.mw.monitor.state.DataType;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author xhy
 * @date 2020/12/19 14:48
 * 排行榜+26
 */
@Component
public class ItemRankModel extends BaseModel {

    private static final Logger logger = LoggerFactory.getLogger("cn/mw/monitor/screen/ItemRankModel");
    private static int i=0;

    @Override
    public void process(ModelContentDto model) {
//        super.executorService = new ThreadPoolExecutor(9, 20, 3, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy());
//        executorService.allowCoreThreadTimeOut(true);
        String modelDataId = model.getModelDataId();
        Integer userId = model.getUserId();
        logger.info("yuzhi1"+model.getModelId());
        switch (model.getModelId()) {
            case 13:
                logger.info("Thread.currentThread()1111111{}", Thread.currentThread());
                processItemWebTimeRank(model, modelDataId, userId);
                break;
            case 14:
                processItemWebInRank(model, modelDataId, userId);
                break;
            case 26:
                processHostCountBylogger(model, modelDataId, userId);
                break;
            default:
                processHostRank(model, modelDataId, userId);
                break;
        }

       /* if (model.getModelId() == 13) {
            processItemWebTimeRank(model, modelDataId, userId);

        } else if (model.getModelId() == 14) {
            processItemWebInRank(model, modelDataId, userId);
        } else if (model.getModelId() == 26) {
            processHostCountBylogger(model, modelDataId, userId);
        } else {
            processHostRank(model, modelDataId, userId);
        }*/

    }

    private void processHostRank(ModelContentDto model, String modelDataId, Integer userId) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                logger.info("yuzhi2");
                threadHashMap.put(userId + modelDataId, Thread.currentThread());
                logger.info("yuzhi3 "+threadHashMap);
                logger.info("yuzhi4 "+WebSocket.sessionPool.get(userId + modelDataId));
                while (null != WebSocket.sessionPool.get(userId + modelDataId)) {
                    logger.info("{getAlertEvent_getHostRank}", modelDataId);
                    try {
                        logger.info("yuzhi5  redisTemplate");
                        String redislist = redisTemplate.opsForValue().get(genRedisKey("getAlertEvent_getHostRank", modelDataId, userId));
                        ItemRank itemRank = new ItemRank();
                        if (null != redislist && StringUtils.isNotEmpty(redislist)) {
                            itemRank = JSONObject.parseObject(redislist, ItemRank.class);
                            logger.info("yuzhi6 "+itemRank);
                        } else {
                            FilterAssetsParam filterAssetsParam = FilterAssetsParam.builder().modelId(model.getModelId()).modelDataId(modelDataId).userId(userId).type(DataType.SCREEN.getName()).build();
                            itemRank = mwModelManage.getHostRank(model.getModelContent(), filterAssetsParam);
                            logger.info("yuzhi7 "+itemRank);
                        }
                        logger.info("yuzhi8 "+itemRank);
                        webSocket.sendObjMessage(userId, modelDataId, itemRank);
                        Thread.sleep(1000 * model.getTimeLag());
                    } catch (Exception e) {
                        logger.error("线程执行InterruptedException{}", e);
                        break;
                    }
                }
            }
        };
        executorService.execute(runnable);
    }

    private void processHostCountBylogger(ModelContentDto model, String modelDataId, Integer userId) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                threadHashMap.put(userId + modelDataId, Thread.currentThread());
                while (null != WebSocket.sessionPool.get(userId + modelDataId) && !Thread.currentThread().isInterrupted()) {
                    try {
                        logger.info("{getHostCountBylogger}", modelDataId);
                        FilterAssetsParam filterAssetsParam = FilterAssetsParam.builder().modelId(26).userId(userId).type(DataType.INDEX.getName()).build();
                        ItemRank itemRank = mwModelManage.getHostCountByLog(filterAssetsParam);
                        webSocket.sendObjMessage(userId, modelDataId, itemRank);
                        Thread.sleep(1000 * model.getTimeLag());
                    } catch (Exception e) {
                        logger.error("线程执行InterruptedException{}", e);
                        break;
                    }
                }
            }
        };
        executorService.execute(runnable);
    }

    private void processItemWebInRank(ModelContentDto model, String modelDataId, Integer userId) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                threadHashMap.put(userId + modelDataId, Thread.currentThread());
                while (null != WebSocket.sessionPool.get(userId + modelDataId)) {
                    try {
                        logger.info("{getItemWebBpsRank_itemWebBpsRank}", modelDataId);
                        String redislist = redisTemplate.opsForValue().get(genRedisKey("getItemWebBpsRank_itemWebBpsRank", modelDataId, userId));
                        ItemRank itemRank = new ItemRank();
                        if (null != redislist && StringUtil.isNotEmpty(redislist)) {
                            itemRank = JSONObject.parseObject(redislist, ItemRank.class);
                        } else {
                            FilterAssetsParam filterAssetsParam = FilterAssetsParam.builder().modelId(model.getModelId()).modelDataId(modelDataId).userId(userId).type(DataType.SCREEN.getName()).build();
                            itemRank = mwModelManage.getItemWebRank(filterAssetsParam, "in");
                        }
                        webSocket.sendObjMessage(userId, modelDataId, itemRank);

                        Thread.sleep(1000 * model.getTimeLag());
                    } catch (Exception e) {
                        logger.error("线程执行InterruptedException{}", e);
                        break;
                    }
                }
            }
        };
        executorService.execute(runnable);
    }

    private void processItemWebTimeRank(ModelContentDto model, String modelDataId, Integer userId) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                threadHashMap.put(userId + modelDataId, Thread.currentThread());
                logger.info("Thread.currentThread()222222222222{}", Thread.currentThread());
                while (null != WebSocket.sessionPool.get(userId + modelDataId)) {
                    try {
                        String redislist = redisTemplate.opsForValue().get(genRedisKey("getItemWebRespRank_itemWebRespRank", modelDataId, userId));
                        ItemRank itemRank = new ItemRank();
                        if (null != redislist && StringUtil.isNotEmpty(redislist)) {
                            itemRank = JSONObject.parseObject(redislist, ItemRank.class);
                        } else {
                            FilterAssetsParam filterAssetsParam = FilterAssetsParam.builder().modelId(model.getModelId()).modelDataId(modelDataId).userId(userId).type(DataType.SCREEN.getName()).build();
                            itemRank = mwModelManage.getItemWebRank(filterAssetsParam, "time");
                        }
                        webSocket.sendObjMessage(userId, modelDataId, itemRank);
                        logger.info("Thread.currentThread()44444444444{}", Thread.currentThread());
                        Thread.sleep(1000 * model.getTimeLag());
                    } catch (Exception e) {
                        logger.error("线程执行InterruptedException{}", e);
                        break;
                    }
                }
            }
        };
        executorService.execute(runnable);
        i=i+1;
        logger.error("{======================================== i++}{}",i);
    }

}
