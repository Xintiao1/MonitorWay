package cn.mw.monitor.api.param.user;

import cn.mw.monitor.service.user.model.MwUserControlSetting;
import cn.mw.monitor.validator.group.Insert;
import cn.mw.monitor.validator.group.Update;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.GroupSequence;
import javax.validation.constraints.*;
import java.util.Date;
import java.util.List;

@Data
@GroupSequence({Insert.class,Update.class, RegisterParam.class})
@ApiModel(description = "添加和更新用户数据")
public class RegisterParam {

    // 用户ID
    @ApiModelProperty("用户id")
    private Integer userId;
    // 登录名
    @ApiModelProperty("登录名")
    private String loginName;
    // 姓名
    @ApiModelProperty("姓名")
    private String userName;
    // 所属用户机构
    @ApiModelProperty("所属用户机构")
    private List<List<Integer>> department;
    // 所属用户组
    @ApiModelProperty("所属用户组")
    private List<Integer> userGroup;
    // 原始密码
    @ApiModelProperty("原始密码")
    @Size(max = 20, message = "原始密码最大长度不能超过20字符！", groups = {Insert.class,Update.class})
    private String oldPassword;
    // 密码
    @ApiModelProperty("密码")
    @Size(max = 20, message = "密码最大长度不能超过20字符！", groups = {Insert.class,Update.class})
    private String password;
    // 电话号码
    @ApiModelProperty("电话号码")
    @Size(max = 100, message = "电话号码最大长度不能超过100字符！", groups = {Insert.class,Update.class})
    private String phoneNumber;
    // 微信
    @ApiModelProperty("微信")
    @Size(max = 100, message = "微信最大长度不能超过20字符！", groups = {Insert.class,Update.class})
    private String wechatId;
    // 钉钉ID
    @ApiModelProperty("钉钉ID")
    @Size(max = 20, message = "钉钉ID最大长度不能超过20字符！", groups = {Insert.class,Update.class})
    private String ddId;
    // 邮箱
    @ApiModelProperty("邮箱")
    @Email(message = "邮箱格式有误!")
    @Size(max = 128, message = "邮箱最大长度不能超过128字符！", groups = {Insert.class,Update.class})
    private String email;
    // 用户过期状态
    @ApiModelProperty("用户过期状态")
    private String userExpireState;
    // 用户过期日期
    @ApiModelProperty("用户过期时间")
    private Date userExpiryDate;
    // 用户访问控制
    @ApiModelProperty("用户访问控制")
    private Boolean userControlEnable;
    // 用户状态,1-启用,-1禁用
    @ApiModelProperty("用户状态")
    private String userState;
    // 角色ID
    @ApiModelProperty("角色ID")
    private Integer roleId;
    // 生效密码策略
    @ApiModelProperty("生效密码策略")
    private Integer activePasswdPlan;
    // 默认密码策略
    @ApiModelProperty("默认密码策略")
    private Integer defaultPasswdPlan;
    // 密码状态
    @ApiModelProperty("密码状态")
    private String passwdState;
    // 密码过期日期
    @ApiModelProperty("密码过期日期")
    private Date passwdExpiryDate;
    // 登录状态
    @ApiModelProperty("登录状态")
    private String loginState;
    // 描述
    @ApiModelProperty("描述")
    private String userDesc;
    // 头像
    @ApiModelProperty("头像")
    private String userImg;
    // 创建时间
    @ApiModelProperty("创建时间")
    private Date createTime;
    // 修改时间
    @ApiModelProperty("修改时间")
    private Date updateTime;

    // 有效期至  长期有效/指定时间
    @ApiModelProperty("有效期至")
    private String validityType;

    private List<String> subscribeRuleIds;
    private List<String> subscribeModelSystem;


    // 加密盐
    @ApiModelProperty("加密盐")
    private String salt;
    // hash类型
    @ApiModelProperty("hash类型")
    private String hashTypeId;
    @ApiModelProperty("机构ID")
    private Integer orgId;
    private String token;

    @ApiModelProperty("个人设置")
    private Boolean personFlag = false;

    //用户登录控制信息
    @ApiModelProperty("用户登录控制信息")
    private MwUserControlSetting controlSettings;
    @ApiModelProperty("用户登录控制条件")
    private String conditionsValue;
    @ApiModelProperty("用户登录控制动作")
    private String actionValue;
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

    private String oa;
}
