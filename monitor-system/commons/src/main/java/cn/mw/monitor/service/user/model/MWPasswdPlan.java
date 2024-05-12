package cn.mw.monitor.service.user.model;

import lombok.Data;

import java.util.Date;

@Data
public class MWPasswdPlan {

    // 密码策略id
    private Integer passwdId;
    // 密码策略名
    private String passwdName;
    // 密码最小长度
    private Integer passwdMinLen;
    // 密码复杂度id
    private Integer passwdComplexId;
    // 是否开启密码历史检查
    private Boolean hisCheckEnable;
    // 保留几次密码记录
    private Integer hisNum;
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
    // 加密盐值
    private String salt;
    // 散列类型id
    private String hashTypeId;
    // 数据版本号（默认为0）
    private Integer version;
    // 密码状态：ACTIVE/LOCK（默认ACTIVE）
    private String passwdState;
    // 创建人
    private String creator;
    // 修改人
    private String modifier;
    // 创建时间
    private Date createDate;
    // 修改时间
    private Date modificationDate;
    // 删除标识符（默认false）
    private Boolean deleteFlag;

    /**
     * 密码到期类型：true:永不到期 false：规定时间内到期
     */
    private Boolean passwdExpireType;

}