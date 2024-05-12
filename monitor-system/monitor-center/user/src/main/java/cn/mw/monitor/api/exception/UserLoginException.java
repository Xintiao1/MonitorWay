package cn.mw.monitor.api.exception;

import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.service.user.exception.UserException;
import lombok.Data;

@Data
public class UserLoginException extends UserException{
    private int code;
    public UserLoginException(){
        super("用户登录异常");
        this.code = ErrorConstant.USER_100125;
    }
}
