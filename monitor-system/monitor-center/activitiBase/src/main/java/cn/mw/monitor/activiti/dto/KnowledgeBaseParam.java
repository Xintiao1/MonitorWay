package cn.mw.monitor.activiti.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author syt
 * @Date 2020/10/9 17:17
 * @Version 1.0
 */
@Data
public class KnowledgeBaseParam {

    @ApiModelProperty(name = "加密id")
    private String id;

    @ApiModelProperty(name = "知识标题")
    private String title;

    @ApiModelProperty(name = "触发原因")
    private String triggerCause;

    @ApiModelProperty(name = "附件地址")
    private String attachmentUrl;

    @ApiModelProperty(name = "解决方案")
    private String solution;

    @ApiModelProperty(name = "知识分类")
    private Integer typeId;

    @ApiModelProperty(name = "创建人")
    private String creator;

    @ApiModelProperty(name = "创建时间")
    private Date createDate;

    @ApiModelProperty(name = "修改人")
    private String modifier;

    @ApiModelProperty(name = "修改时间")
    private Date modificationDate;

    @ApiModelProperty(name = "删除标识符")
    private Boolean deleteFlag;

    @ApiModelProperty(name = "版本号")
    private Integer version;

    @ApiModelProperty(name = "流程状态")
    private Integer activitiStatus;

    @ApiModelProperty(name = "流程实例id")
    private String processId;

    @ApiModelProperty(name = "类型名称")
    private String typeName;
}
