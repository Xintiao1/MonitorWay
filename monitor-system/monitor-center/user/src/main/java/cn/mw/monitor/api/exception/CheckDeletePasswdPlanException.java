package cn.mw.monitor.api.exception;

import cn.mw.monitor.common.constant.ErrorConstant;
import lombok.Data;

@Data
public class CheckDeletePasswdPlanException extends RuntimeException {

    private String msg;

    private String passwdNames;

    private int code;

    public CheckDeletePasswdPlanException(String msg, String passwdNames) {
        super(msg + passwdNames);
        this.code = ErrorConstant.PASSWDPLAN_100605;
        this.msg = msg;
        this.passwdNames = passwdNames;
    }

    public CheckDeletePasswdPlanException(int code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

}
