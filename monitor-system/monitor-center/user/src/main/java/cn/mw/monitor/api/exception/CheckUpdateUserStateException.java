package cn.mw.monitor.api.exception;

import lombok.Data;

@Data
public class CheckUpdateUserStateException extends RuntimeException {

    private String msg;

    private int code;

    public CheckUpdateUserStateException(int code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

}
