package cn.mw.monitor.knowledgeBase.dto;

import cn.mw.monitor.service.knowledgeBase.model.MwKnowledgeBaseTable;
import lombok.Data;

import java.util.List;

/**
 * @author syt
 * @Date 2020/9/4 14:35
 * @Version 1.0
 */
@Data
public class MwKnowledgeBaseTableDTO extends MwKnowledgeBaseTable {
    private List<FileList> fileList;
    //点赞数量
    private Integer likedCount;
    //被踩数量
    private Integer hatedCount;
    //点赞状态
    private Integer likedStatus;

}
