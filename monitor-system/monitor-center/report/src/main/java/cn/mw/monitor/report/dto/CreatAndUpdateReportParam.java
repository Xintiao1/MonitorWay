package cn.mw.monitor.report.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author xhy
 * @date 2020/5/9 16:34
 */
@Data
public class CreatAndUpdateReportParam {
    @ApiModelProperty("报表id")
    private String reportId;
    @ApiModelProperty("报表名称")
    private String reportName;
    @ApiModelProperty("报表描述")
    private String reportDesc;
    @ApiModelProperty("报表类型id")
    private Integer typeNameId;
    @ApiModelProperty("定时任务id")
    private Integer timeTagId;
    @ApiModelProperty("关联动作ids")
    private List<Integer> actionNameIds;
    @ApiModelProperty("关联规则ids")
    private List<String> ruleIds;
    @ApiModelProperty("创建人")
    private String creator;
    @ApiModelProperty("修改人")
    private String modifier;

    @ApiModelProperty("用户列表")
    private List<Integer> userIds;
    @ApiModelProperty(value = "机构")
    private List<List<Integer>> orgIds;
    @ApiModelProperty("用户组列表")
    private List<Integer> groupIds;
    @ApiModelProperty("是否定时发送")
    private Boolean sendTime;

    @ApiModelProperty("通知用户")
    private List<Integer> noticeUser;

    @ApiModelProperty("通知用户组")
    private List<Integer> noticeUserGroup;
}
