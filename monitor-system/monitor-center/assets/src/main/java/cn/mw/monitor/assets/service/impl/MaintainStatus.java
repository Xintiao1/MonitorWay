package cn.mw.monitor.assets.service.impl;

public enum MaintainStatus {
    Using(0,"启用中"), Closing(1,"接近中"), Expire(2,"已过期")
    ,Shielding(3 ,"屏蔽中");
    private int code;
    private String name;

    MaintainStatus(int code ,String name){
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }
}
