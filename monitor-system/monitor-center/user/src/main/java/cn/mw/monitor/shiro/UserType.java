package cn.mw.monitor.shiro;

public enum UserType {
    /**
     * LDAP用户
     */
    LDAP("AD"),
    /**
     * 本地用户
     */
    MW("LOCAL");
    private String type;

    UserType(String type) {
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
