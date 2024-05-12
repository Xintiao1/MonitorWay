package cn.mw.monitor.knowledgeBase.service.impl;

import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.knowledgeBase.dao.MwKnowledgeLikeOrHateRecordDao;
import cn.mw.monitor.knowledgeBase.dao.MwKnowledgeUserMapperDao;
import cn.mw.monitor.knowledgeBase.dto.KnowledgeLikedDTO;
import cn.mw.monitor.knowledgeBase.model.MwKnowledgeLikeOrHateRecord;
import cn.mw.monitor.knowledgeBase.model.MwKnowledgeUserMapper;
import cn.mw.monitor.knowledgeBase.service.RedisService;
import cn.mw.monitor.knowledgeBase.util.RedisKeyUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author syt
 * @Date 2020/9/11 15:09
 * @Version 1.0
 */
@Service
@Slf4j
public class RedisServiceImpl implements RedisService {
    private static final Logger logger = LoggerFactory.getLogger("cn/mw/monitor/knowledgeBase");
    @Autowired
    StringRedisTemplate redisTemplate;
    @Resource
    private MwKnowledgeUserMapperDao mwKnowledgeUserMapperDao;
    @Resource
    private MwKnowledgeLikeOrHateRecordDao mwKnowledgeLikeOrHateRecordDao;


    @Override
    public Reply saveLikedStatusRedis(String knowledgeId, int userId, Integer status) {
        try {
            String key = RedisKeyUtils.getLikedKey(knowledgeId, userId);
            redisTemplate.opsForHash().put(RedisKeyUtils.MAP_KEY_KNOWLEDGE_LIKED, key, status.toString());
            if (status > 0) {
                //如果状态大于0 ，点赞或者踩数量加一
                incrementLikedCount(knowledgeId, status);
            } else {
                //如果状态小于0 ，点赞或者踩数量减一
                status = -status;
                decrementLikedCount(knowledgeId, status);
            }
            return Reply.ok();
        } catch (Exception e) {
            log.error("fail to saveLikedStatusRedis cause:{}", e.getMessage());
            return Reply.fail(ErrorConstant.KNOWLEDGE_LIKED_STATUS_REDIS_SAVE_CODE_309007, ErrorConstant.KNOWLEDGE_LIKED_STATUS_REDIS_SAVE_MSG_309007);
        }
    }

    @Override
    public Reply deleteLikedFromRedis(String knowledgeId, int userId) {
        try {
            String key = RedisKeyUtils.getLikedKey(knowledgeId, userId);
            redisTemplate.opsForHash().delete(RedisKeyUtils.MAP_KEY_KNOWLEDGE_LIKED, key);
            return Reply.ok();
        } catch (Exception e) {
            log.error("fail to deleteLikedFromRedis cause:{}", e.getMessage());
            return Reply.fail(ErrorConstant.KNOWLEDGE_LIKED_REDIS_DELETE_CODE_309008, ErrorConstant.KNOWLEDGE_LIKED_REDIS_DELETE_MSG_309008);
        }
    }

    @Override
    public Reply incrementLikedCount(String knowledgeId, int status) {
        try {
            String key = RedisKeyUtils.getLikedStatusKey(knowledgeId, status);
            redisTemplate.opsForHash().increment(RedisKeyUtils.MAP_KEY_KNOWLEDGE_LIKED_COUNT, key, 1);
            return Reply.ok();
        } catch (Exception e) {
            log.error("fail to incrementLikedCount cause:{}", e.getMessage());
            return Reply.fail(ErrorConstant.KNOWLEDGE_LIKED_REDIS_UPDATE_CODE_309009, ErrorConstant.KNOWLEDGE_LIKED_REDIS_UPDATE_MSG_309009);
        }
    }

    @Override
    public Reply decrementLikedCount(String knowledgeId, int status) {
        try {
            String key = RedisKeyUtils.getLikedStatusKey(knowledgeId, status);
            redisTemplate.opsForHash().increment(RedisKeyUtils.MAP_KEY_KNOWLEDGE_LIKED_COUNT, key, -1);
            return Reply.ok();
        } catch (Exception e) {
            log.error("fail to decrementLikedCount cause:{}", e.getMessage());
            return Reply.fail(ErrorConstant.KNOWLEDGE_LIKED_REDIS_UPDATE_CODE_309009, ErrorConstant.KNOWLEDGE_LIKED_REDIS_UPDATE_MSG_309009);
        }
    }

    @Override
    public Reply getLikedDataFromRedis() {
        try {
            Cursor<Map.Entry<Object, Object>> scan = redisTemplate.opsForHash().scan(RedisKeyUtils.MAP_KEY_KNOWLEDGE_LIKED, ScanOptions.NONE);
            List<MwKnowledgeUserMapper> list = new ArrayList<>();
            while (scan.hasNext()) {
                Map.Entry<Object, Object> entry = scan.next();
                String key = (String) entry.getKey();
                String[] split = key.split("::");
                String knowledgeId = split[0];
                Integer userId = Integer.valueOf(split[1]);
                Integer status = Integer.parseInt(entry.getValue().toString());
                //组装成MwKnowledgeUserMapper对象
                MwKnowledgeUserMapper mwKnowledgeUserMapper = new MwKnowledgeUserMapper(knowledgeId, userId, status);
                list.add(mwKnowledgeUserMapper);
                //存到list后从Redis中删除
                redisTemplate.opsForHash().delete(RedisKeyUtils.MAP_KEY_KNOWLEDGE_LIKED, key);
            }
            return Reply.ok(list);
        } catch (Exception e) {
            log.error("fail to getLikedDataFromRedis cause:{}", e.getMessage());
            return Reply.fail(ErrorConstant.KNOWLEDGE_LIKED_REDIS_SELECTALL_CODE_309010, ErrorConstant.KNOWLEDGE_LIKED_REDIS_SELECTALL_MSG_309010);
        }
    }

    @Override
    public Reply getLikedCountFromRedis() {
        try {
            Cursor<Map.Entry<Object, Object>> scan = redisTemplate.opsForHash().scan(RedisKeyUtils.MAP_KEY_KNOWLEDGE_LIKED_COUNT, ScanOptions.NONE);
            List<MwKnowledgeLikeOrHateRecord> list = new ArrayList<>();
            while (scan.hasNext()) {
                Map.Entry<Object, Object> entry = scan.next();
                String key = (String) entry.getKey();
                String[] split = key.split("::");
                String knowledgeId = split[0];
                Integer status = Integer.valueOf(split[1]);
                Integer times = Integer.parseInt(entry.getValue().toString());
                //组装成MwKnowledgeUserMapper对象
                MwKnowledgeLikeOrHateRecord mwKnowledgeLikeOrHateRecord = new MwKnowledgeLikeOrHateRecord(knowledgeId, status, times);
                list.add(mwKnowledgeLikeOrHateRecord);
                //存到list后从Redis中删除
                redisTemplate.opsForHash().delete(RedisKeyUtils.MAP_KEY_KNOWLEDGE_LIKED_COUNT, key);
            }
            return Reply.ok(list);
        } catch (Exception e) {
            log.error("fail to getLikedCountFromRedis cause:{}", e.getMessage());
            return Reply.fail(ErrorConstant.KNOWLEDGE_LIKED_REDIS_SELECT_COUNTALL_CODE_309011, ErrorConstant.KNOWLEDGE_LIKED_REDIS_SELECT_COUNTALL_MSG_309011);
        }
    }

    @Override
    public Reply getLikedStatusAndCount(String knowledgeId, int userId) {
        try {
            KnowledgeLikedDTO knowledgeLikedDTO = new KnowledgeLikedDTO();
            //先从redis中查找
            String dataKey = RedisKeyUtils.getLikedKey(knowledgeId, userId);
            Object redisData = redisTemplate.opsForHash().get(RedisKeyUtils.MAP_KEY_KNOWLEDGE_LIKED, dataKey);
            // 获取知识点赞数量
            Integer likedCount = getLikedCount(knowledgeId, 1);
            //获取知识被踩数量
            Integer hatedCount = getLikedCount(knowledgeId, 2);
            if (redisData != null) {
                //说明redis中有数据
                knowledgeLikedDTO.setStatus(Integer.parseInt(redisData.toString()));
            } else {
                //从数据库中查
                List<Integer> statuss = mwKnowledgeUserMapperDao.selectStatus(knowledgeId, userId);
                if (statuss.size() > 0) {
                    knowledgeLikedDTO.setStatus(statuss.get(0));
                }
            }
            //从数据库中查点赞数量
            Integer times = mwKnowledgeLikeOrHateRecordDao.selectTimes(knowledgeId, 1);
            times = (times == null ? 0 : times) + likedCount;
            knowledgeLikedDTO.setLikedCount(times == null ? 0 : times);
            //从数据库中查被踩数量
            Integer hatedTimes = mwKnowledgeLikeOrHateRecordDao.selectTimes(knowledgeId, 2);
            hatedTimes = (hatedTimes == null ? 0 : hatedTimes) + hatedCount;
            knowledgeLikedDTO.setHatedCount(hatedTimes == null ? 0 : hatedTimes);
            return Reply.ok(knowledgeLikedDTO);
        } catch (Exception e) {
            log.error("fail to getLikedCountFromRedis cause:{}", e.getMessage());
            return Reply.fail(ErrorConstant.KNOWLEDGE_LIKED_REDIS_SELECT_COUNTALL_CODE_309011, ErrorConstant.KNOWLEDGE_LIKED_REDIS_SELECT_COUNTALL_MSG_309011);
        }
    }

    /**
     * 获取redis中的点赞数量
     *
     * @param knowledgeId
     * @param status
     * @return
     */
    @Override
    public Integer getLikedCount(String knowledgeId, int status) {
        String countKey = RedisKeyUtils.getLikedStatusKey(knowledgeId, status);
        Object redisCount = redisTemplate.opsForHash().get(RedisKeyUtils.MAP_KEY_KNOWLEDGE_LIKED_COUNT, countKey);
        if (redisCount != null) {
            return Integer.parseInt(redisCount.toString());
        }
        return 0;
    }
}
