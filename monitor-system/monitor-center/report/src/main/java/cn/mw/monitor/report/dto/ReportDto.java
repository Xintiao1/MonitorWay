package cn.mw.monitor.report.dto;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * @author xhy
 * @date 2020/5/9 16:59
 */
public class ReportDto {
    @ApiModelProperty("报表id")
    private String id;
    @ApiModelProperty("报表名称")
    private String reportName;
    @ApiModelProperty("报表描述")
    private String reportDesc;
    @ApiModelProperty("报表类型id")
    private Integer typeId;
    @ApiModelProperty("定时任务id")
    private Integer timeId;
    @ApiModelProperty("关联动作ids")
    private List<Integer> actionIds;
    @ApiModelProperty("创建人")
    private String creator;
    @ApiModelProperty("修改人")
    private String modifier;
    @ApiModelProperty("是否有用户权限")
    private Boolean isUser;
    @ApiModelProperty("是否有用户组权限")
    private Boolean isGroup;
}
