package cn.mw.monitor.knowledgeBase.dto;

import cn.mw.monitor.service.knowledgeBase.model.MwKnowledgeBaseTable;
import lombok.Data;

import java.util.List;

/**
 * @author syt
 * @Date 2020/9/4 17:15
 * @Version 1.0
 */
@Data
public class DeleteKnowledgeParam {
    private List<String> ids;
    private List<MwKnowledgeBaseTable> knowledgeBaseTables;

}
