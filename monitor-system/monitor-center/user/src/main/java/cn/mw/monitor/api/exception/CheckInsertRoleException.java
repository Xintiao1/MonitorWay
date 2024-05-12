package cn.mw.monitor.api.exception;

import lombok.Data;

@Data
public class CheckInsertRoleException extends RuntimeException {

    private String msg;

    private int code;

    public CheckInsertRoleException(int code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

}
