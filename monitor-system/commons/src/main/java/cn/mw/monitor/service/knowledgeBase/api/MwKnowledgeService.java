package cn.mw.monitor.service.knowledgeBase.api;

import cn.mw.monitor.service.knowledgeBase.model.MwKnowledgeBaseTable;

import java.util.List;
import java.util.Map;

/**
 * @author syt
 * @Date 2020/10/12 9:20
 * @Version 1.0
 */
public interface MwKnowledgeService {

    void editorActivitiParam(String processId, Integer activitiStatus, String knowledgeId);

    MwKnowledgeBaseTable selectByProcessId(String processId);

    MwKnowledgeBaseTable selectById(String id);

    //根据知识库ids获取点赞数量或者被踩数量 (status:0 点赞；1 被踩)
    Map<String, Integer> getLikeOrHateListCount(List<String> knowledgeIds, int status);

    //根据知识库id获取点赞数量或者被踩数量 (status:0 点赞；1 被踩)
    Integer getLikeOrHateCount(String knowledgeId, int status);
}
