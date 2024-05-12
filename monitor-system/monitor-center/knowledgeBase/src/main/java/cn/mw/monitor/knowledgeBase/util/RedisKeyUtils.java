package cn.mw.monitor.knowledgeBase.util;

/**
 * @author syt
 * @Date 2020/8/20 16:58
 * @Version 1.0
 */
public class RedisKeyUtils {
    //保存知识点赞数据的key
    public static final String MAP_KEY_KNOWLEDGE_LIKED = "MAP_KNOWLEDGE_LIKED";
    //保存知识被点赞数量的key
    public static final String MAP_KEY_KNOWLEDGE_LIKED_COUNT = "MAP_KNOWLEDGE_LIKED_COUNT";

    /**
     * 拼接被点赞的知识id和点赞的状态作为key,格式22222::1
     *
     * @param knowledgeId 被点赞的知识id
     * @param status      点赞状态
     * @return
     */
    public static String getLikedStatusKey(String knowledgeId, int status) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(knowledgeId);
        stringBuilder.append("::");
        stringBuilder.append(status);
        return stringBuilder.toString();
    }
    /**
     * 拼接被点赞的知识id和点赞的人的id作为key,格式22222::33333
     * @param knowledgeId 被点赞的知识id
     * @param userId      点赞的用户id
     * @return
     */
    public static String getLikedKey(String knowledgeId, int userId) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(knowledgeId);
        stringBuilder.append("::");
        stringBuilder.append(userId);
        return stringBuilder.toString();
    }

}
