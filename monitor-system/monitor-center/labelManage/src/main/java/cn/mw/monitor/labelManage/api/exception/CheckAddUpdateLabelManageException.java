package cn.mw.monitor.labelManage.api.exception;

public class CheckAddUpdateLabelManageException extends RuntimeException {

    private String msg;

    private int code;

    public CheckAddUpdateLabelManageException(int code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

}
