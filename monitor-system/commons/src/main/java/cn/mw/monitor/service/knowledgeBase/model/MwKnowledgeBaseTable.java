package cn.mw.monitor.service.knowledgeBase.model;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author syt
 * @Date 2020/8/19 14:13
 * @Version 1.0
 */
@Data
public class MwKnowledgeBaseTable {

    @ApiModelProperty(name = "知识id")
    @ExcelProperty(value = {"知识id"})
    private String id;

    @ApiModelProperty(name = "知识标题")
    @ExcelProperty(value = {"知识标题"})
    private String title;

    @ApiModelProperty(name = "触发原因")
    @ExcelProperty(value = {"触发原因"})
    private String triggerCause;

    @ApiModelProperty(name = "附件地址")
    @ExcelProperty(value = {"附件地址"})
    private String attachmentUrl;

    @ApiModelProperty(name = "解决方案")
    @ExcelProperty(value = {"解决方案"})
    private String solution;

    @ApiModelProperty(name = "知识分类")
    @ExcelProperty(value = {"知识分类"})
    private Integer typeId;

    @ApiModelProperty(name = "创建人")
    @ExcelProperty(value = {"创建人"})
    private String creator;

    @ApiModelProperty(name = "创建时间")
    @ExcelProperty(value = {"创建时间"})
    private Date createDate;

    @ApiModelProperty(name = "修改人")
    @ExcelProperty(value = {"修改人"})
    private String modifier;

    @ApiModelProperty(name = "修改时间")
    @ExcelProperty(value = {"修改时间"})
    private Date modificationDate;

    @ApiModelProperty(name = "删除标识符")
    @ExcelProperty(value = {"删除标识符"})
    private Boolean deleteFlag;

    @ApiModelProperty(name = "版本号")
    @ExcelProperty(value = {"版本号"})
    private Integer version;

    @ApiModelProperty(name = "流程状态")
    @ExcelProperty(value = {"流程状态"})
    private Integer activitiStatus;

    @ApiModelProperty(name = "流程实例id")
    @ExcelProperty(value = {"流程实例id"})
    private String processId;

    @ApiModelProperty(name = "类型名称")
    @ExcelProperty(value = {"类型名称"})
    private String typeName;

}
