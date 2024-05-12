package cn.mw.monitor.api.exception;

public class CheckInsertPasswdPlanException extends RuntimeException {
    private String msg;

    private int code;

    public CheckInsertPasswdPlanException(int code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }
}
