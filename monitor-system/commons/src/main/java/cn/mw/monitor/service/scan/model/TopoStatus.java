package cn.mw.monitor.service.scan.model;

public enum TopoStatus {
    Exception(0,"异常"),Doing(1,"进行中"), Done(2,"已完成");

    private int code;
    private String name;

    TopoStatus(int code, String name){
        this.code = code;
        this.name = name;
    }
}
