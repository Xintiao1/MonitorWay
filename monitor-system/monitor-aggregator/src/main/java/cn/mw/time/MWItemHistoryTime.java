package cn.mw.time;

import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.manager.dto.MwAssetsIdsDTO;
import cn.mw.monitor.server.service.impl.MwServerManager;
import cn.mw.monitor.service.assets.model.RedisItemHistoryDto;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author syt
 * @Date 2020/5/28 15:56
 * @Version 1.0
 */
@Component
//@EnableScheduling //定时器类注解
//@ConditionalOnProperty(prefix = "scheduling", name = "enabled", havingValue = "true")
@Slf4j
public class MWItemHistoryTime {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private MWTPServerAPI mwtpServerAPI;
    @Autowired
    private MwServerManager mwServerManager;
    @Autowired
    private MwAssetsManager mwAssetsManager;

    private static final Map<Integer, String> timeTypeValue = new HashMap<>();

    static {
        timeTypeValue.put(1, "15mins:");
        timeTypeValue.put(2, "60mins:");
    }

//    @Scheduled(cron = "0 */15 * * * ?") //15分钟执行一次 将item信息存入redis
    public void saveItemHistory() {
        log.info(">>>>>>>saveItemHistory>>>>>>>>>>");
        saveItemsHistory(1, 672);
        log.info(">>>>>>>saveItemHistory>>>>>>>>>>");
    }

//    @Scheduled(cron = "0 */60 * * * ?") //1小时执行一次 将item信息存入redis
    public void saveItemHistoryHour() {
        log.info(">>>>>>>saveItemHistoryHour>>>>>>>>>>");
        saveItemsHistory(2, 720);
        log.info(">>>>>>>saveItemHistoryHour>>>>>>>>>>");
    }

    @Async
    public void saveToRedis(String key, RedisItemHistoryDto historyByTime, int timeCount) {
        String value = "";
        if (historyByTime != null) {
            value = JSONObject.toJSONString(historyByTime);
        }
        if (redisTemplate.hasKey(key)) {
            Long count = redisTemplate.opsForZSet().zCard(key);
            //取出最后一个value值
            Set<String> range = redisTemplate.opsForZSet().range(key, count - 1, count);
            List<RedisItemHistoryDto> list = new ArrayList<>();
            for (String str : range) {
                if (null != str && StringUtils.isNotEmpty(str)) {
                    RedisItemHistoryDto redisItemHistoryDto = JSONObject.parseObject(str, RedisItemHistoryDto.class);
                    list.add(redisItemHistoryDto);
                }
            }
            if (list.size() > 0) {
                //跟最后一个值跟当前值做比较，获取最大的（平均，最大，最小）
                historyByTime = mwServerManager.compareValue(list.get(0), historyByTime);
                value = JSONObject.toJSONString(historyByTime);
            }
            if (count >= timeCount) {
                long l = count - timeCount;
                redisTemplate.opsForZSet().removeRange(key, 0, l);
                count = 0L;
            }
            redisTemplate.opsForZSet().add(key, value, count);
        } else {
            redisTemplate.opsForZSet().add(key, value, 0);
        }

    }

    @Async
    public void saveItemsHistory(int timeType, int timeCount) {
        List<MwAssetsIdsDTO> assetsIds = mwAssetsManager.getAssetsIds(false);
        if (assetsIds != null && !assetsIds.isEmpty()) {
            assetsIds.forEach(assetsId -> {
                //筛选监控项信息类型为0或者3,关联映射值的id为0的主机为assetsId.getHostId()下的监控项
                int monitorServerId = assetsId.getMonitorServerId();
                MWZabbixAPIResult items = mwtpServerAPI.getItemsByHostIdFilter(monitorServerId, assetsId.getHostId(), Arrays.asList("0", "3"), "0",null,null);
                if (!items.isFail()) {
                    JsonNode map = (JsonNode) items.getData();
                    if (map.size() > 0) {
                        for (JsonNode item : map) {
                            //获取监控项n分钟数据的平均值
                            String type = item.get("type").asText();
                            String key = "";
                            if (type.equals("9")) {//type==9 是web监测的监控项
                                key = assetsId.getId() + ":" + timeTypeValue.get(timeType) + item.get("itemid").asText() + item.get("key_").asText();
                            } else {
                                key = assetsId.getId() + ":" + timeTypeValue.get(timeType) + item.get("itemid").asText() + item.get("name").asText();
                            }
                            RedisItemHistoryDto historyByTime = mwServerManager.getHistoryByTime(monitorServerId, item.get("itemid").asText(), timeType, item.get("value_type").asInt());
                            if (historyByTime != null && historyByTime.getAvgValue() != null) {
                                saveToRedis(key, historyByTime, timeCount);
                            }
                        }
                    }
                }
            });
        }
    }

    public void initUpdateItemsHistory() {
        List<MwAssetsIdsDTO> assetsIds = mwAssetsManager.getAssetsIds(false);
        if (assetsIds != null && !assetsIds.isEmpty()) {
            assetsIds.forEach(assetsId -> {
                //筛选监控项信息类型为0或者3,关联映射值的id为0的主机为assetsId.getHostId()下的监控项
                int monitorServerId = assetsId.getMonitorServerId();
                MWZabbixAPIResult items = mwtpServerAPI.getItemsByHostIdFilter(monitorServerId, assetsId.getHostId(), Arrays.asList("0", "3"), "0", null,null);
                if (!items.isFail()) {
                    JsonNode map = (JsonNode) items.getData();
                    if (map.size() > 0) {
                        for (JsonNode item : map) {
                            //获取原先key值
                            for (int i = 1; i < 3; i++) {
                                String oldKey = timeTypeValue.get(i) + item.get("itemid").asText() + item.get("name").asText();
                                String newKey = assetsId.getId() + ":" + timeTypeValue.get(i) + item.get("itemid").asText() + item.get("name").asText();
                                if (redisTemplate.hasKey(oldKey)) {
                                    //从排序集中获取开始和结束之间的元组
                                    Set<ZSetOperations.TypedTuple<String>> typedTuples = redisTemplate.opsForZSet().rangeWithScores(oldKey, 0, -1);
                                    //存到新的key中
                                    redisTemplate.opsForZSet().add(newKey, typedTuples);
                                    redisTemplate.delete(oldKey);
                                }
                            }
                        }
                    }
                }
            });
        }
        Set<String> keys = redisTemplate.keys(timeTypeValue.get(1) + "*");
        keys.addAll(redisTemplate.keys(timeTypeValue.get(2) + "*"));
        keys.forEach(key -> {
            redisTemplate.delete(key);
        });
    }
}
