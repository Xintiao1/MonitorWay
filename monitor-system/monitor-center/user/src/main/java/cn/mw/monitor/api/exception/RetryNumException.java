package cn.mw.monitor.api.exception;

import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.service.user.exception.UserException;
import lombok.Data;

@Data
public class RetryNumException extends UserException{

    private String loginName;
    private int retryNum;
    private int code;

    public RetryNumException(String loginName, int retryNum){
        super(loginName + "尝试超过" + retryNum + "次");
        this.loginName = loginName;
        this.retryNum = retryNum;
        this.code = ErrorConstant.USER_100121;
    }
}
