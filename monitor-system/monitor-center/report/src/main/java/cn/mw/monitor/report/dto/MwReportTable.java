package cn.mw.monitor.report.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author xhy
 * @date 2020/5/11 13:41
 */
@Data
public class MwReportTable {
    @ApiModelProperty("报表id")
    private String reportId;
    private Integer typeNameId;
    private Integer timeTagId;
    private String reportName;
    @ApiModelProperty("报表描述")
    private String reportDesc;
    @ApiModelProperty("报表类型")
    private String typeName;
    @ApiModelProperty("定时任务名称")
    private String timeTag;

    @ApiModelProperty("定时任务通知方式名称")
    private List<String> actionName;
    private List<Integer> actionNameIds;

    @ApiModelProperty("规则通知方式名称")
    private List<String> ruleName;
    private List<String> ruleIds;

    @ApiModelProperty("创建时间")
    private Date createDate;
    @ApiModelProperty("创建人")
    private String creator;
    @ApiModelProperty("修改时间")
    private Date modificationDate;
    @ApiModelProperty("修改人")
    private String modifier;

    private List<UserDTO> principal;

    private List<OrgDTO> department;

    private List<GroupDTO> groups;


    @ApiModelProperty(value="机构")
    private List<List<Integer>> orgIds;

    @ApiModelProperty(value="负责人")
    private List<Integer> userIds;

    @ApiModelProperty(value="用户组")
    private List<Integer> groupIds;

    private boolean sendTime;

    @ApiModelProperty("通知用户")
    private List<Integer> noticeUser;

    @ApiModelProperty("通知用户组")
    private List<Integer> noticeUserGroup;

    @ApiModelProperty("机构字符串")
    private String departmentString;

    @ApiModelProperty("用户组字符串")
    private String groupsString;

    @ApiModelProperty("负责人字符串")
    private String principalString;

}
