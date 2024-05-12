package cn.mw.monitor.alert.param;

public enum UserTypeEnum {

    USER("负责人"),
    GROUP("用户组"),
    ORG("机构");

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    UserTypeEnum(String name){
        this.name = name;
    }



}
