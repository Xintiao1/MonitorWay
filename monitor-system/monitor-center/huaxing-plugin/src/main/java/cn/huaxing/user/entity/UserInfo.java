package cn.huaxing.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * 华星LDAP用户管理表
 *
 * @author guiquanwnag
 * @since 2023-08-24
 */
@TableName("MW_HUAXING_LDAP_USER")
@Data
public class UserInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID，用户ID
     */
    @TableId(value = "USER_ID", type = IdType.INPUT)
    private Integer id;

    /**
     * 用户名称
     */
    @TableField("USER_NAME")
    private String userName;

    /**
     * 登录名称
     */
    @TableField("LOGIN_NAME")
    private String loginName;

    /**
     * 手机号
     */
    @TableField("PHONE_NUMBER")
    private String phoneNumber;

    /**
     * 微信ID
     */
    @TableField("WECHAT_ID")
    private String weChatId;

    /**
     * 邮箱地址
     */
    @TableField("EMAIL")
    private String email;

    /**
     * 创建时间
     */
    @TableField("CREATE_TIME")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField("UPDATE_TIME")
    private Date updateTime;

    /**
     * 是否删除（true：删除  false：未删除）
     */
    @TableField("DELETE_FLAG")
    private Boolean deleteFlag;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserInfo userInfo = (UserInfo) o;
        return Objects.equals(userName, userInfo.userName) && Objects.equals(loginName, userInfo.loginName) && Objects.equals(phoneNumber, userInfo.phoneNumber) && Objects.equals(weChatId, userInfo.weChatId) && Objects.equals(email, userInfo.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, loginName, phoneNumber, weChatId, email);
    }
}
