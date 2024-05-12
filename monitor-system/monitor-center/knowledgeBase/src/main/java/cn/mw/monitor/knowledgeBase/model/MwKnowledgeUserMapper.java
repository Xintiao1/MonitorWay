package cn.mw.monitor.knowledgeBase.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author syt
 * @Date 2020/8/24 14:33
 * @Version 1.0
 */
@Data
public class MwKnowledgeUserMapper {

    @ApiModelProperty(name = "id")
    private int id;

    @ApiModelProperty(name = "知识id")
    private String knowledgeId;

    @ApiModelProperty(name = "用户id")
    private int userId;

    @ApiModelProperty(name = "状态：-1取消点赞 ，1点赞；-2取消踩，2踩")
    private int status;

    public MwKnowledgeUserMapper(String knowledgeId, int userId, int status) {
        this.knowledgeId = knowledgeId;
        this.userId = userId;
        this.status = status;
    }
}
