package cn.mw.monitor.screen.service.modelImpl;

import cn.mwpaas.common.utils.StringUtils;
import cn.mw.monitor.screen.service.WebSocket;
import cn.mw.monitor.screen.dto.ModelContentDto;
import cn.mw.monitor.screen.dto.SecurityDto;
import cn.mw.monitor.util.MWUtils;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xhy
 * @date 2021/1/11 14:10
 * 23 安全事件类型统计
 */
@Component
public class SecurityTypeCountModel extends BaseModel {
    private static final Logger logger = LoggerFactory.getLogger("cn/mw/monitor/screen/SecurityCountModel");

    @Override
    public void process(ModelContentDto model) {
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
                        logger.info("{getSecurityCountByField_type}", modelDataId);
                        try {
                        String redisValue = redisTemplate.opsForValue().get(MWUtils.REDIS_SECURITY_TYPE);
                        if (null == redisValue) {
                            String typeList = mwModelManage.getTypeList();
                            redisValue = typeList;
                        }
                        List<String> list = JSONArray.parseArray(redisValue, String.class);
                        List<SecurityDto> lists = new ArrayList<>();
                        for (String type : list) {
                            String key = genRedisKey("getSecurityCountByField_type" + type, modelDataId, userId);
                            if (null != key && StringUtils.isNotEmpty(key)) {
                                String redisList = redisTemplate.opsForValue().get(key);
                                if (null != redisList && StringUtil.isNotEmpty(redisList)) {
                                    SecurityDto securityDto = JSONArray.parseObject(redisList, SecurityDto.class);
                                    lists.add(securityDto);
                                }
                            }
                        }
                        if (lists.size() == 0) {
                            lists = (List<SecurityDto>) mwModelManage.getSecurityCountByField("type");
                        }
                        logger.info("SecurityTypeCountModel1 "+lists);
                        webSocket.sendObjMessage(userId,modelDataId, lists);

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
