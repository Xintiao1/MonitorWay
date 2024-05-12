package cn.mw.monitor.api.exception;

import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.service.user.exception.UserException;
import lombok.Data;

@Data
public class UserLoginInfoException extends UserException{
    private int code;
    public UserLoginInfoException(){
        super("获取用户数据异常");
        this.code = ErrorConstant.USER_100125;
    }
}
