package cn.mw.monitor.service.alert.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.List;

/**
 * @author xhy
 * @date 2020/8/13 16:52
 */
@Data
public class AlertRuleTableCommons{
    private String ruleId;
    private String ruleName;
    private String ruleDesc;
    @ApiModelProperty("通知类型 1微信 2钉钉 3邮件 4短信 5其他")
    private Integer actionType;
    private Boolean enable;
    private String testMessage;
    private List<Integer> testUserId;
    private String proxyIp;
    private String proxyPort;
    private String proxyAccount;
    private String proxyPassword;
    private Boolean proxyState;
//    private String creator;
//    private Date createDate;
//    private String modifier;
//    private Date modificationDate;
//    private List<Integer> userIds;
//    private List<Integer> groupIds;
//    private List<List<Integer>> orgIds;
//
//    private List<UserDTO> principal;
//
//    private List<OrgDTO> department;
//
//    private List<GroupDTO> groups;
}
