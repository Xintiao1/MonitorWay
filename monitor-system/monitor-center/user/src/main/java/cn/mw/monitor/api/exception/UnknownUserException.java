package cn.mw.monitor.api.exception;

import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.service.user.exception.UserException;
import lombok.Data;

@Data
public class UnknownUserException extends UserException{
    private int code;
    public UnknownUserException(){
        super("用户名或密码错误");
        this.code = ErrorConstant.USER_100126;
    }
}
