package cn.huaxing.user.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author guiquanwnag
 * @datetime 2023/8/24
 * @Description LDAP用户信息
 */
@Data
public class LdapUserInfo implements Serializable {

    private String userName;

    private String groupName;

    private String loginName;

    private String mail;

    private String phone;

    /**
     * 2023-03-27 用于更新用户微信号
     */
    private String wxNo;

    /**
     * 2023-03-27 用于更新用户钉钉号
     */
    private String dingdingNo;


    /**
     * 2023-03-30 TCL项目，用于更新科长属性（上级）
     */
    private String manager;

    /**
     * 用户状态
     */
    private String userState;

}

