package cn.mw.monitor.api.exception;

import lombok.Data;

@Data
public class CheckUpdateOrgStateException extends RuntimeException {

    private String msg;

    private int code;

    public CheckUpdateOrgStateException(int code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

}
