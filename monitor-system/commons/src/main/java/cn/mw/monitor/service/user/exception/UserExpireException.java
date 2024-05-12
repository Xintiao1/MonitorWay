package cn.mw.monitor.service.user.exception;

import cn.mw.monitor.common.constant.ErrorConstant;
import lombok.Data;

@Data
public class UserExpireException extends UserException{

    private String loginName;
    private int code;

    public UserExpireException(String loginName){
        super(loginName + "用户密码已经过期,请联系系统管理员！");
        this.loginName = loginName;
        this.code = ErrorConstant.USER_100120;
    }
}
