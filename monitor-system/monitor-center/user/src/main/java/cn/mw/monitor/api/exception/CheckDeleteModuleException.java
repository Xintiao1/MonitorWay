package cn.mw.monitor.api.exception;

import cn.mw.monitor.common.constant.ErrorConstant;
import lombok.Data;

@Data
public class CheckDeleteModuleException extends RuntimeException{
    private String msg;

    private String moduleNames;

    private int code;

    public CheckDeleteModuleException(String msg, String moduleNames) {
        super(msg + moduleNames);
        this.code = ErrorConstant.ORG_100402;
        this.msg = msg;
        this.moduleNames = moduleNames;
    }

}
