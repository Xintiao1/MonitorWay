package cn.mw.monitor.service.user.exception;

import cn.mw.monitor.common.constant.ErrorConstant;
import lombok.Data;

@Data
public class RetrySecException extends UserException{
    private String loginName;
    private int retrySec;
    private int code;

    public RetrySecException(String loginName, int retrySec){
        super(loginName + "尝试间隔低于" + retrySec + "秒");
        this.loginName = loginName;
        this.retrySec = retrySec;
        this.code = ErrorConstant.USER_100121;
    }
}
