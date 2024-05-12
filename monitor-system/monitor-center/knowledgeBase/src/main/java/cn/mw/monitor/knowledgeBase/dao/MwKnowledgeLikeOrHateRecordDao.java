package cn.mw.monitor.knowledgeBase.dao;

import cn.mw.monitor.knowledgeBase.model.MwKnowledgeLikeOrHateRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author syt
 * @Date 2020/9/14 9:47
 * @Version 1.0
 */
public interface MwKnowledgeLikeOrHateRecordDao {
    int insert(MwKnowledgeLikeOrHateRecord record);

    List<MwKnowledgeLikeOrHateRecord> selectList(Map record);

    int delete(List<String> knowledgeIds);

    List<MwKnowledgeLikeOrHateRecord> saveAll(List<MwKnowledgeLikeOrHateRecord> list);

    Integer selectTimes(@Param("knowledgeId") String knowledgeId, @Param("status") Integer status);

    MwKnowledgeLikeOrHateRecord selectByKnowledgeIdAndStatus(@Param("knowledgeId") String knowledgeId, @Param("status") Integer status);

    int updateTimes(@Param("times") Integer times, @Param("knowledgeId") String knowledgeId, @Param("status") Integer status);
}
