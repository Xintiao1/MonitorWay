package cn.huaxing.user.entity;

import lombok.Data;

/**
 * @author gui.quanwang
 * @className AdUserSyncParam
 * @description AD用户自定义参数
 * @date 2023/4/14
 */
@Data
public class AdUserSyncParam {

    /**
     * 用户名称（姓名）
     */
    private String userName;

    /**
     * 登录名称
     */
    private String loginName;

    /**
     * 手机号
     */
    private String phone;
    /**
     * 邮箱
     */
    private String mail;
    /**
     * 企业微信号
     */
    private String wxNo;
    /**
     * 钉钉号
     */
    private String dingdingNo;
    /**
     * 用户状态
     */
    private String userState;
    /**
     * 经理信息
     */
    private String manager;

}
