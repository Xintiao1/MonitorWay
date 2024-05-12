package cn.mw.monitor.util;


public enum LicenseEnum {
    //1，订阅式到期不停用；2：授权式到期停用；3：测试许可到期停用
    subscribed(1)
    ,authorized(2)
    ,test(3);

    private Integer chNum;

    LicenseEnum(Integer chNum){
        this.chNum = chNum;
    }

    public Integer getChNum() {
        return chNum;
    }
}
