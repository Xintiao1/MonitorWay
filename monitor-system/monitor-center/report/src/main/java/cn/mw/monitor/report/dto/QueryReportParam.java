package cn.mw.monitor.report.dto;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author xhy
 * @date 2020/5/9 16:17
 */
@Data
public class QueryReportParam extends BaseParam {
    @ApiModelProperty("报表名称")
    private String reportName;
    @ApiModelProperty("报表描述")
    private String reportDesc;
    @ApiModelProperty("报表类型id")
    private Integer reportTypeId;
    @ApiModelProperty("定时任务")
    private String timeTaskId;
    @ApiModelProperty("创建开始时间")
    private Date createDateStartStart;
    @ApiModelProperty("创建结束时间")
    private Date createDateStartEnd;
    @ApiModelProperty("更新开始时间")
    private Date createDateEndStart;
    @ApiModelProperty("更新结束时间")
    private Date createDateEndEnd;
    @ApiModelProperty("创建人")
    private String creator;
    @ApiModelProperty("修改人")
    private String modifier;

    private String perm;
    private Integer userId;
    private List<Integer> groupIds;
    private List<Integer> orgIds;

    private Boolean isAdmin;

}
