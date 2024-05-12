package cn.mw.monitor.api.exception;

import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.service.user.exception.UserException;
import lombok.Data;

@Data
public class LoginNameException extends UserException{
    private int code;
    public LoginNameException(){
        super("用户名不能为空");
        this.code = ErrorConstant.USER_100125;
    }
}
