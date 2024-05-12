package cn.mw.monitor.api.exception;

import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.service.user.exception.UserException;
import lombok.Data;

@Data
public class UserControlException extends UserException{
    private int code;
    public UserControlException(){
        super(ErrorConstant.USER_MSG_100130);
        this.code = ErrorConstant.USER_100130;
    }
}
