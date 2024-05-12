package cn.mw.monitor.service.user.dto;

import lombok.Data;

@Data
public class SettingDTO {
    private String logoUrl;
    private String icon;
    private String userIcon;
    private String logoBasecode;
    private String sideheaderColor;
    private String sidemenuColor;
    private String sidemenuTextcolor;
    private String sidemenuTextSelectcolor;
    private String localLanguage;
    private String title;
    private String titleColor;
    private String logoDescrition;
    private String httpHeader;

    /**
     * 用户自定义头像（如果用户已登录）
     */
    private String userImg;

    /**
     * 登录方式
     */
    private String loginType;

    /**
     * 跳转页面
     */
    private String redirectUrl;
}
