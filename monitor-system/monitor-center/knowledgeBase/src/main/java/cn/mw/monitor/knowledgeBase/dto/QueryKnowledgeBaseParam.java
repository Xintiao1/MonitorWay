package cn.mw.monitor.knowledgeBase.dto;

import cn.mw.monitor.bean.BaseParam;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author syt
 * @Date 2020/8/20 14:23
 * @Version 1.0
 */
@Data
public class QueryKnowledgeBaseParam extends BaseParam {

    @ApiModelProperty(name = "知识id")
    @ExcelProperty(value = {"知识id"})
    private String id;

    @ApiModelProperty(name = "知识标题")
    @ExcelProperty(value = {"知识标题"})
    private String title;

    @ApiModelProperty(name = "触发原因")
    @ExcelProperty(value = {"触发原因"})
    private String triggerCause;

//    @ApiModelProperty(name = "附件地址")
//    private String attachmentUrl;

    @ApiModelProperty(name = "知识分类")
    @ExcelProperty(value = {"知识分类"})
    private Integer typeId;

    @ApiModelProperty(name = "知识分类")
    @ExcelProperty(value = {"知识分类"})
    private List<Integer> typeIds;

    @ApiModelProperty(name = "解决方案")
    @ExcelProperty(value = {"解决方案"})
    private String solution;

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

    @ApiModelProperty(name = "创建时间查询开始")
    private Date createDateStart;
    @ApiModelProperty(name = "创建时间查询结束")
    private Date createDateEnd;
    @ApiModelProperty(name = "修改时间查询开始")
    private Date modificationDateStart;
    @ApiModelProperty(name = "修改时间查询结束")
    private Date modificationDateEnd;

    @ApiModelProperty(name = "是否查询点赞情况")
    private Boolean giveFlag;

    @ApiModelProperty(name = "流程状态")
    private Integer activitiStatus;

    @ApiModelProperty(name = "流程实例id")
    private String processId;

    private Integer userId;

    private String fuzzyQuery;
    private String value;
}
