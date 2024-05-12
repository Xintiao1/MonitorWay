package cn.mw.monitor.knowledgeBase.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author syt
 * @Date 2020/9/11 17:18
 * @Version 1.0
 */
@Data
public class MwKnowledgeLikeOrHateRecord {

    @ApiModelProperty(name = "id")
    private int id;

    @ApiModelProperty(name = "知识id")
    private String knowledgeId;

    @ApiModelProperty(name = "状态：-1取消点赞 ，1点赞；-2取消踩，2踩")
    private int status;

    //点赞数量
    private Integer times;

    public MwKnowledgeLikeOrHateRecord(String knowledgeId, int status, Integer times) {
        this.knowledgeId = knowledgeId;
        this.status = status;
        this.times = times;
    }
}
