package cn.mw.monitor.knowledgeBase.dao;

import cn.mw.monitor.knowledgeBase.model.MwKnowledgeUserMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author syt
 * @Date 2020/8/24 14:14
 * @Version 1.0
 */
public interface MwKnowledgeUserMapperDao {

    int insert(MwKnowledgeUserMapper record);

    List<MwKnowledgeUserMapper> selectList(Map record);

    List<Integer> selectByKnowledgeIdAndStatus(@Param("knowledgeId") String knowledgeId, @Param("status") Integer status);

    List<Integer> selectStatus(@Param("knowledgeId") String knowledgeId, @Param("userId") Integer userId);

    MwKnowledgeUserMapper selectLikedByKnowledgeIdAndUserId(@Param("knowledgeId") String knowledgeId, @Param("userId") Integer userId);

    int updateStatus(@Param("status") Integer status, @Param("knowledgeId") String knowledgeId, @Param("userId") Integer userId);

    int delete(List<String> knowledgeIds);

    List<MwKnowledgeUserMapper> saveAll(List<MwKnowledgeUserMapper> list);
}
