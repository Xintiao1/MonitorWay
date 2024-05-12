package cn.mw.monitor.api.exception;

import cn.mw.monitor.common.constant.ErrorConstant;

public class CheckInsertGroupException extends RuntimeException{
    private String msg;

    private int code;

    public CheckInsertGroupException(String msg) {
        super(msg);
        this.code = ErrorConstant.USERGROUPCODE_250114;
        this.msg = msg;
    }
}
