package cn.mw.monitor.screen.service.modelImpl;

import cn.mw.monitor.screen.service.WebSocket;
import cn.mw.monitor.screen.dto.ModelContentDto;
import cn.mw.monitor.screen.dto.SecurityDto;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.util.StringUtil;
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
 * @date 2021/1/11 15:17
 *
 * 21 安全事件级别统计
 */
@Component
public class SecurityAlertLevelModel extends BaseModel {
    private static final Logger logger = LoggerFactory.getLogger("cn/mw/monitor/screen/SecurityAlertLevelModel");

    @Override
    public  void process(ModelContentDto model) {
//        ThreadPoolExecutor executorService = new ThreadPoolExecutor(9, 20, 3, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy());
//        executorService.allowCoreThreadTimeOut(true);
        String modelDataId = model.getModelDataId();
        Integer userId = model.getUserId();

        int timeLag = dao.getBulkDataTimeCount(modelDataId, userId) == 0 ? 600 : dao.getBulkDataTime(modelDataId, userId);
        try {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    threadHashMap.put(userId+modelDataId,Thread.currentThread());
                    while (null != WebSocket.sessionPool.get(userId +modelDataId)) {
                        logger.info("{getSecurityCountByField_alertLevel}", modelDataId);
                        try {
                        String key1 = genRedisKey("getSecurityCountByField_alertLevel" + 1, modelDataId, userId);
                        String key2 = genRedisKey("getSecurityCountByField_alertLevel" + 2, modelDataId, userId);
                        String key3 = genRedisKey("getSecurityCountByField_alertLevel" + 3, modelDataId, userId);
                        String key4 = genRedisKey("getSecurityCountByField_alertLevel" + 4, modelDataId, userId);
                        String key5 = genRedisKey("getSecurityCountByField_alertLevel" + 5, modelDataId, userId);
                        String redisList1 = redisTemplate.opsForValue().get(key1);
                        String redisList2 = redisTemplate.opsForValue().get(key2);
                        String redisList3 = redisTemplate.opsForValue().get(key3);
                        String redisList4 = redisTemplate.opsForValue().get(key4);
                        String redisList5 = redisTemplate.opsForValue().get(key5);
                        List<SecurityDto> list = new ArrayList<>();
                        if (null != redisList1 && StringUtil.isNotEmpty(redisList1)) {
                            list.add(JSONArray.parseObject(redisList1, SecurityDto.class));
                        }
                        if (null != redisList2 && StringUtil.isNotEmpty(redisList2)) {
                            list.add(JSONArray.parseObject(redisList2, SecurityDto.class));
                        }
                        if (null != redisList3 && StringUtil.isNotEmpty(redisList3)) {
                            list.add(JSONArray.parseObject(redisList3, SecurityDto.class));
                        }
                        if (null != redisList4 && StringUtil.isNotEmpty(redisList4)) {
                            list.add(JSONArray.parseObject(redisList4, SecurityDto.class));
                        }
                        if (null != redisList5 && StringUtil.isNotEmpty(redisList5)) {
                            list.add(JSONArray.parseObject(redisList5, SecurityDto.class));
                        }
                        if (list.size() == 0) {
                            list = (List<SecurityDto>) mwModelManage.getSecurityCountByField("alertLevel");
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
