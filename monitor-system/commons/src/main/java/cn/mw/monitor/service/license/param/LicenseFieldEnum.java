package cn.mw.monitor.service.license.param;

public enum LicenseFieldEnum {


    DATE("date");

    private String name;
    LicenseFieldEnum(String name){
        this.name = name;
    }
    public String toString() {
        return name;
    }
}
