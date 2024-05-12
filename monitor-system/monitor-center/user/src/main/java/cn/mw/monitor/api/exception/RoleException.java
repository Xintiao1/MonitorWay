package cn.mw.monitor.api.exception;

public class RoleException extends RuntimeException{

    private int code;

    public RoleException(int code, String msg){
        super(msg);
        this.code = code;
    }
}
