package cn.mw.monitor.service.action.param;

import lombok.Data;
import java.util.List;

/**
 * @author xhy
 * @date 2020/8/26 9:18
 */
@Data
public class AddAndUpdateAlertActionParam extends CommonsParam {
    public static final String MESSAGECONTEXT_KEY = "AddAndUpdateAlertActionParam";

    private String actionId;
    private String actionName;
    private List<Integer> actionTypeIds;
    private List<String> ruleIds;
    private Boolean isAllAssets = true;
    //0：选择用户；1:默认选择；2：自定义
    private Integer isAllUser;
    private List<String> userTypes;
    private Integer assetsTypeId;
    private String assetsId;
    private String assetsName;
    private String inBandIp;
    private Integer assetsTypeSubId;
    private String pollingEngine;
    private Integer monitorMode;
    private String manufacturer;
    private String specifications;



    private Label label;

//    private String labelName;
//    private Integer labelId;
//    private Integer inputFormat;
//    private String labelValue;
//    private String dropdownValue;
//    private String dropKey;
//    private Date labelDateStart;
//    private Date labelDateEnd;
//    private List<Date> labelTimeValue;

    private List<String> severity;
  //  private String severity;
    private List<Integer> actionUserIds;

    private List<Integer> actionGroupIds;

    private List<UserDTO> actionUsers;

    private Boolean enable;

    private int state;//分级告警
    private List<Integer> levelTwoUserIds;//分级告警二级用户组
    private float date;//分级告警
    private List<Integer> levelThreeUserIds;//分级告警三级用户组

    private String area;
    private String email;

}
