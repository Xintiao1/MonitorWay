package cn.mw.monitor.service.user.dto;

import cn.mw.monitor.common.bean.BaseDTO;
import cn.mw.monitor.service.user.model.MwUserControlSetting;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("用户传输对象")
public class UserDTO extends BaseDTO {

    // 用户ID
    @ApiModelProperty("用户id")
    private Integer userId;
    // 登录名
    @ApiModelProperty("登录名")
    private String loginName;
    // 姓名
    @ApiModelProperty("姓名")
    private String userName;
    // 密码
    @ApiModelProperty("原始密码")
    private String oldPassword;
    // 密码
    @ApiModelProperty("密码")
    private String password;
    // 电话号码
    @ApiModelProperty("电话号码")
    private String phoneNumber;
    // 微信公众号
    @ApiModelProperty("微信")
    private String wechatId;
    // 钉钉ID
    @ApiModelProperty("钉钉id")
    private String ddId;
    // 邮箱
    @ApiModelProperty("邮箱")
    private String email;
    // 用户过期日期
    @ApiModelProperty("用户过期日期")
    private Date userExpiryDate;
    //用户微信openid
    @ApiModelProperty("用户微信openid")
    private String openId;


    // 角色ID
    @ApiModelProperty("角色id")
    private Integer roleId;
    @ApiModelProperty("角色名")
    private String roleName;
    // 加密盐
    @ApiModelProperty("加密盐值")
    private String salt;
    // hash类型
    @ApiModelProperty("hash类型")
    private String hashTypeId;
    // 用户状态,1-启用,-1禁用
    @ApiModelProperty("用户状态")
    private String userState;
    // 创建时间
    @ApiModelProperty("创建时间")
    private Date createTime;
    // 修改时间
    @ApiModelProperty("修改时间")
    private Date updateTime;
    // 登录状态
    @ApiModelProperty("登录状态")
    private String loginState;
    // 描述
    @ApiModelProperty("描述")
    private String userDesc;
    // 头像
    @ApiModelProperty("头像")
    private String userImg;
    // 生效密码策略
    @ApiModelProperty("生效密码策略")
    private Integer activePasswdPlan;
    // 默认密码策略
    @ApiModelProperty("默认密码策略")
    private Integer defaultPasswdPlan;
    // 密码状态
    @ApiModelProperty("密码状态")
    private String passwdState;
    // 用户过期状态
    @ApiModelProperty("用户过期状态")
    private String userExpireState;
    // 密码过期日期
    @ApiModelProperty("密码过期日期")
    private Date passwdExpiryDate;
    // 用户访问控制
    @ApiModelProperty("用户访问控制")
    private Boolean userControlEnable;
    // 用户组IDs
    @ApiModelProperty("用户组ids")
    private List<Integer> userGroup;
    // 机构IDs
    @ApiModelProperty("机构ids")
    private List<List<Integer>> department;

    @ApiModelProperty("个人设置")
    private Boolean personFlag;
    //用户登录控制信息
    @ApiModelProperty("用户登录控制信息")
    private MwUserControlSetting controlSettings;
    @ApiModelProperty("用户登录控制条件")
    private String conditionsValue;
    @ApiModelProperty("用户登陆控制动作")
    private String actionValue;

    // 有效期至  长期有效/指定时间
    @ApiModelProperty("有效期至")
    private String validityType;
    //临时表中密码策略id
    @ApiModelProperty("临时表中密码策略id")
    private Integer inoperactivePasswdPlan;

    private List<String> subscribeRuleIds;
    private List<String> subscribeModelSystem;

    //批量编辑判断
    private Boolean batchEditor = false;
    private List<Integer> batchUserIds;
    private Boolean checkUserState;
    private Boolean checkUserControlEnable;
    private Boolean checkUserGroup;
    private Boolean checkRoleId;
    private Boolean checkActivePasswdPlan;
    private Boolean checkValidityType;
    private Boolean checkDepartment;

    private String userType;

    /**
     * 更多手机号
     */
    private String morePhones;

    private String oa;
}
