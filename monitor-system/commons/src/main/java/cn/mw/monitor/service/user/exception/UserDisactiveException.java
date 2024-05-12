package cn.mw.monitor.service.user.exception;

import cn.mw.monitor.common.constant.ErrorConstant;
import lombok.Data;

@Data
public class UserDisactiveException extends UserException{

    private String loginName;
    private int code;

    public UserDisactiveException(String loginName){
        super(loginName + "已被禁用");
        this.loginName = loginName;
        this.code = ErrorConstant.USER_100127;
    }
}
