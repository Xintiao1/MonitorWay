package cn.mw.monitor.knowledgeBase.dto;

import lombok.Data;

/**
 * @author syt
 * @Date 2020/9/14 10:59
 * @Version 1.0
 */
@Data
public class KnowledgeLikedDTO {
    private Integer status;
    private Integer likedCount;
    private Integer hatedCount;
}
