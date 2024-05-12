package cn.mw.monitor.knowledgeBase.service;

import cn.mwpaas.common.model.Reply;

/**
 * @author syt
 * @Date 2020/9/11 14:40
 * @Version 1.0
 */
public interface RedisService {
    /**
     * 将用户，知识，点赞状态存入redis
     *
     * @param knowledgeId
     * @param userId
     * @param status
     */
    Reply saveLikedStatusRedis(String knowledgeId, int userId, Integer status);

    /**
     * 从Redis中删除一条点赞数据
     *
     * @param knowledgeId
     * @param userId
     */
    Reply deleteLikedFromRedis(String knowledgeId, int userId);

    /**
     * 该知识的点赞或者被踩加一
     *
     * @param knowledgeId
     */
    Reply incrementLikedCount(String knowledgeId, int status);

    /**
     * 该知识的点赞或者被踩减一
     *
     * @param knowledgeId
     */
    Reply decrementLikedCount(String knowledgeId, int status);

    /**
     * 获取Redis中存储的所有点赞和被踩数据
     *
     * @return
     */
    Reply getLikedDataFromRedis();

    /**
     * 获取Redis中存储的所有点赞和被踩数量
     *
     * @return
     */
    Reply getLikedCountFromRedis();

    /**
     * 查询当前用户和知识的点赞状态，点赞数量
     *
     * @return
     */
    Reply getLikedStatusAndCount(String knowledgeId, int userId);

    /**
     * 获取redis中的点赞数量
     *
     * @param knowledgeId
     * @param status
     * @return
     */
    Integer getLikedCount(String knowledgeId, int status);
}
