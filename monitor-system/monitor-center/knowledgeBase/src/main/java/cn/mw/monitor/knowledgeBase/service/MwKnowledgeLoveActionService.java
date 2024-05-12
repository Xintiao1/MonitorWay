package cn.mw.monitor.knowledgeBase.service;

import cn.mwpaas.common.model.Reply;

/**
 * @author syt
 * @Date 2020/9/11 11:49
 * @Version 1.0
 */
public interface MwKnowledgeLoveActionService {
    /**
     * 将Redis里的点赞数据存入数据库中
     *
     * @return
     */
    Reply transLikedFromRedisToMysql();

    /**
     * 将Redis里的点赞数量数据存入数据库中
     *
     * @return
     */
    Reply transLikedCountFromRedisToMysql();


}
