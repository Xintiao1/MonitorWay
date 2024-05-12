package cn.mw.monitor.shiro;

import lombok.Data;

/**
 *
 * @author zy.quaee
 * @date 2021/4/30 9:14
 **/
public enum LoginType {
    /**
     * 通用
     */
    Common("mw_login"),

    /**
     * ldap登录
     */
    LDAP_LOGIN("ldap_login");

    private String type;

    LoginType(String type) {
        this.type = type;
    }
    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return this.type.toString();
    }
}
