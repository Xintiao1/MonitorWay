package cn.mw.monitor.alert.param;

import cn.mw.monitor.bean.DataPermissionParam;
import cn.mw.monitor.state.DataType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import cn.mw.monitor.service.assets.model.GroupDTO;

import java.util.Date;
import java.util.List;
import cn.mw.monitor.service.assets.model.UserDTO;
import cn.mw.monitor.service.user.dto.OrgDTO;

/**
 * @author xhy
 * @date 2020/8/13 16:52
 */
@Data
public class AlertRuleTable extends DataPermissionParam {
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
    private String creator;
    private Date createDate;
    private String modifier;
    private Date modificationDate;
    private List<Integer> userIds;
//    private List<Integer> groupIds;
//    private List<List<Integer>> orgIds;
    private List<UserDTO> userDTO;

    private String userDTOString;
//    private List<UserDTO> principal;
//
    private List<OrgDTO> department;

    private String departmentString;
//
    private List<GroupDTO> groups;

    private String groupsString;
    @Override
    public DataType getBaseDataType() {
        return DataType.RULE;
    }

    @Override
    public String getBaseTypeId() {
        return ruleId + "";
    }

    private Boolean isHtml;

}
