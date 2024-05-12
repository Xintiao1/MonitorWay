package cn.mw.monitor.knowledgeBase.dto;

import lombok.Data;

/**
 * @author syt
 * @Date 2020/8/20 11:40
 * @Version 1.0
 */
@Data
public class KnowledgeTypeDTO {
    //类型id
    private Integer typeId;
    //类型名
    private String typeName;
    //知识库的总数量
    private Integer kCount;
    //类型对应小图标的名称
    private String urlName;
    //知识库状态值;
    private Integer activitiStatus;

}
