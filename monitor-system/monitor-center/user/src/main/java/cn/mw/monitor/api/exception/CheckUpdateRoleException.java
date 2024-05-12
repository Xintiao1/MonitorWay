package cn.mw.monitor.api.exception;

public class CheckUpdateRoleException extends RuntimeException {

    private String msg;

    private int code;

    public CheckUpdateRoleException(int code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }
}
