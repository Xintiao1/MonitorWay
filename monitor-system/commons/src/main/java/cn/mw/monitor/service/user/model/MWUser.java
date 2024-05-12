package cn.mw.monitor.service.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MWUser {

    // 用户ID
    private Integer userId;
    // 登录名
    private String loginName;
    // 姓名
    private String userName;
    // 密码
    private String password;
    // 电话号码
    private String phoneNumber;
    // 微信公众号
    private String wechatId;
    // 钉钉ID
    private String ddId;
    // 邮箱
    private String email;
    // 用户过期状态
    private String userExpireState;
    // 用户过期日期
    private Date userExpiryDate;
    // 用户状态,1-启用,-1禁用
    private String userState;
    // 用户访问控制
    private Boolean userControlEnable;
    // 生效密码策略
    private Integer activePasswdPlan;
    // 默认密码策略
    private Integer defaultPasswdPlan;
    // 密码状态
    private String passwdState;
    // 密码过期日期
    private Date passwdExpiryDate;
    // 描述
    private String userDesc;
    // 头像
    private String userImg;
    // 用户登录状态
    private String loginState;
    // 创建人
    private String creator;
    // 创建时间
    private Date createTime;
    // 修改人
    private String modifier;
    // 修改时间
    private Date updateTime;
    // 删除标识
    private Boolean deleteFlag;
    //用户登录控制信息
    private MwUserControlSetting controlSettings;
    //用户登录控制条件  满足-1  不满足-2
    private String conditionsValue;
    //用户登录控制动作  允许-1  不允许-2
    private String actionValue;
    // 有效期至  长期有效/指定时间
    private String validityType;
    //临时表中密码策略id
    private Integer inoperactivePasswdPlan;
    //临时表中修改类型
    private Integer modifyType;

    //用户类型
    private String userType;
    //AD组用户
    private String adUserGroupName;
    //AD域名
    private String domainName;

    /**
     * 更多手机号
     */
    private String morePhones;
    private String oa;
}
