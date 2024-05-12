package cn.mw.monitor.alert.param;

import cn.mw.monitor.bean.BaseParam;
import cn.mw.monitor.bean.DataPermissionParam;
import cn.mw.monitor.state.DataType;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author xhy
 * @date 2020/8/13 16:20
 */
@Data
public class MwAlertRuleParam extends DataPermissionParam {
    private String ruleId;
    private String ruleName;
    private String ruleDesc;
    private Integer actionType;
    private String creator;
    private String modifier;
    private Date createDateStart;
    private Date createDateEnd;
    private Date modificationDateStart;
    private Date modificationDateEnd;
    private List<Integer> userIds;
    private List<Integer> groupIds;
    //private List<Integer> orgIds;
    private Integer userId;
    private Boolean isAdmin;

    private List<Integer> actionTypeIds;
    private String fuzzyQuery;

    @Override
    public DataType getBaseDataType() {
        return DataType.RULE;
    }

    @Override
    public String getBaseTypeId() {
        return ruleId + "";
    }
}
