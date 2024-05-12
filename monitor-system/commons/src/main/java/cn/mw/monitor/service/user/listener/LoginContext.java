package cn.mw.monitor.service.user.listener;

import cn.mw.monitor.service.user.dto.LoginDTO;
import cn.mw.monitor.service.user.dto.LoginInfo;
import cn.mw.monitor.service.user.dto.MWPasswordPlanDTO;
import cn.mw.monitor.service.user.model.MwRoleModulePermMapper;
import cn.mw.monitor.state.PasswdState;
import cn.mw.monitor.state.UserActiveState;
import cn.mw.monitor.state.UserExpireState;
import lombok.Data;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Data
public class LoginContext {

    // 密码更新日期
    private Integer passwdUpdateDate;
    // 过期前几天警告用户
    private Integer expireAlertDay;
    // 到期后,经过几天，必须修改密码
    private Integer afterResetDay;
    // 是否可以锁定用户
    private Boolean lockEnable;
    // 密码过期后几天,强制锁定
    private Integer afterLockDay;
    // 首次登录是否强制修改
    private Boolean firstPasswdEnable;
    // 是否强制用户修改密码
    private Boolean changePasswdEnable;
    // 密码是否可以重置
    private Boolean resetEnable;
    // 尝试登录失败后拒绝访问
    private Boolean isRefuseAcc;
    // 登录失败尝试登录次数
    private Integer retryNum;
    // 多少秒过后，允许再次访问
    private Integer retrySec;
    // 数据版本号（默认为0）
    private Integer version;
    // 密码状态：ACTIVE/LOCK（默认ACTIVE）
    private PasswdState passwdState;

    private String dbpasswd;

    private MWPasswordPlanDTO mwpasswordPlanDTO;

    private AtomicInteger retryCount;

    private Integer userId;

    private Date loginTime;

    private String loginState;

    private UserExpireState userExpireState;

    private Date passwdExpiryDate;

    private String loginName;

    private Boolean userControlEnable;
    // 用户启用状态
    private UserActiveState userState;
    //
    private Date userExpiryDate;

    private LoginDTO loginDTO;

    private LoginInfo loginInfo;

    private Map<String, MwRoleModulePermMapper> mwRoleModulePermMapper;

    //true- 登录次数超过密码策略次数锁定  false- 密码过期锁定
    private Boolean lockType = true;

}
