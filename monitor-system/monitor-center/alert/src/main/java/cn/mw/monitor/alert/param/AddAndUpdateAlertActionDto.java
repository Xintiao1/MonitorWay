package cn.mw.monitor.alert.param;

import cn.mw.monitor.bean.DataPermissionParam;
import cn.mw.monitor.state.DataType;
import lombok.Data;
import java.util.Date;
import java.util.List;

/**
 * @author xhy
 * @date 2020/8/26 9:18
 */
@Data
public class AddAndUpdateAlertActionDto extends DataPermissionParam{
    private String actionId;
    private String actionName;
    private List<Integer> actionTypeIds;
    private List<String> ruleIds;
    //0：选择用户；1:默认选择；2：自定义
    private Integer isAllUser;
    private List<String> userTypes;
    private List<String> severity;

    private List<Integer> actionUserIds;

    private List<Integer> actionGroupIds;


    private Boolean enable;

    private int state;//分级告警
    private List<String> levelOneRuleIds;
    //1:默认选择；2：自定义
    private Integer levelOneIsAllUser;
    private String levelOneEmail;
    private List<Integer> levelOneUserIds;//分级告警一级用户组
    private float levelOneDate;
    private Boolean isSendPersonOne;
    private Integer oneTime;
    private List<String> levelTwoRuleIds;
    //1:默认选择；2：自定义
    private Integer levelTwoIsAllUser;
    private String levelTwoEmail;
    private List<Integer> levelTwoUserIds;//分级告警二级用户组
    private float levelTwoDate;//分级告警
    private Integer twoTime;
    private Boolean isSendPersonTwo;
    private List<String> levelThreeRuleIds;
    //1:默认选择；2：自定义
    private Integer levelThreeIsAllUser;
    private String levelThreeEmail;
    private List<Integer> levelThreeUserIds;//分级告警三级用户组
    private float levelThreeDate;
    private Integer threeTime;
    private Boolean isSendPersonThree;
    private List<AssetsFielidParam> assetsFielid;
    private Integer level;

    private String effectTimeSelect;//生效时间下拉框
    private String startTime;//开始时间
    private String endTime;//结束时间
    private String alarmCompressionSelect; //告警压缩下拉框
    private Integer customTime;//自定义时间
    private String timeUnit;//时间单位
    private Integer customNum;//自定义次数
    private String numUnit;//自定义次数单位
    private Integer successNum;//成功次数
    private Integer failNum;//失败次数
    private Integer count;//匹配总数；
    private String area;
    private String email;
    private String creator;
    private String modifier;
    private List<Integer> userIds;

    @Override
    public DataType getBaseDataType() {
        return DataType.ACTION;
    }

    @Override
    public String getBaseTypeId() {
        return actionId + "";
    }

}
