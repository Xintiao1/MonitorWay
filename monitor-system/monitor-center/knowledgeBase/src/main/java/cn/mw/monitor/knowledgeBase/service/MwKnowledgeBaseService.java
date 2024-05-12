package cn.mw.monitor.knowledgeBase.service;

import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.knowledgeBase.dto.AddOrUpdateKnowledgeBaseParam;
import cn.mw.monitor.knowledgeBase.dto.DeleteKnowledgeParam;
import cn.mw.monitor.knowledgeBase.dto.QueryKnowledgeBaseParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author syt
 * @Date 2020/8/19 17:33
 * @Version 1.0
 */
public interface MwKnowledgeBaseService {
    /**
     * 查询知识库具体信息
     * @param id
     * @param giveFlag  是否查点赞被踩状态，数量
     * @return
     */
    Reply selectById(String id, Boolean giveFlag);

    Reply selectTableList(QueryKnowledgeBaseParam qParam);

    Reply update(AddOrUpdateKnowledgeBaseParam uParam) throws Exception;

    Reply delete(DeleteKnowledgeParam param) throws Exception;

    Reply insert(AddOrUpdateKnowledgeBaseParam aParam);

    Reply getTypeTree(String type);

    Reply updateMyKnowledge(AddOrUpdateKnowledgeBaseParam uParam);

    Reply deleteMyKnowledge(DeleteKnowledgeParam param);

    Reply insertMyKnowledge(AddOrUpdateKnowledgeBaseParam aParam);

    Reply fuzzSearchAllFiledData(QueryKnowledgeBaseParam param);

    Reply exportTableList(QueryKnowledgeBaseParam qParam, HttpServletResponse response);

    Reply templateInfoImport(MultipartFile file, HttpServletResponse response) throws IOException;
}
