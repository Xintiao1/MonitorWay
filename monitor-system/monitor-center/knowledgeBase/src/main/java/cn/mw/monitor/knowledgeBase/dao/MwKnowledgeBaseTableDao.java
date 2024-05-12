package cn.mw.monitor.knowledgeBase.dao;

import cn.mw.monitor.knowledgeBase.dto.AddOrUpdateKnowledgeBaseParam;
import cn.mw.monitor.knowledgeBase.dto.TypeTreeDTO;
import cn.mw.monitor.service.knowledgeBase.model.MwKnowledgeBaseTable;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author syt
 * @Date 2020/8/19 14:10
 * @Version 1.0
 */
public interface MwKnowledgeBaseTableDao {
    int delete(List<String> id);

    MwKnowledgeBaseTable selectById(String id);

    int insert(AddOrUpdateKnowledgeBaseParam aParam);

    int update(AddOrUpdateKnowledgeBaseParam uParam);

    List<MwKnowledgeBaseTable> selectTableList(Map record);

    List<TypeTreeDTO> selectTypeClassByPId(int pid);

    int selectVersionById(String id);

    int editorActivitiStatus(@Param("activitiStatus") Integer activitiStatus, @Param("knowledgeId") String knowledgeId);

    int editorActivitiParam(@Param("processId") String processId, @Param("activitiStatus") Integer activitiStatus, @Param("knowledgeId") String knowledgeId);

    List<MwKnowledgeBaseTable> selectList(@Param("creator") String creator, @Param("activitiStatus") Integer activitiStatus);

    MwKnowledgeBaseTable selectByProcessId(String processId);

    List<Map<String,String>> fuzzSearchAllFiled(@Param("activitiStatus") Integer activitiStatus,@Param("value") String value);

}
