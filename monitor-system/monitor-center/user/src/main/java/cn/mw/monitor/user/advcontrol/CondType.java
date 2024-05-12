package cn.mw.monitor.user.advcontrol;

public enum CondType {
    AllSatisfy(1,"AllSatisfy"), AllNotSatisfy(2,"AllNotSatisfy");
    private int code;
    private String name;

    CondType(int code, String name){
        this.code = code;
        this.name = name;
    }
}
