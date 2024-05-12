package cn.mw.monitor.service.user.exception;

import cn.mw.monitor.common.constant.ErrorConstant;
import lombok.Data;
import org.omg.CORBA.UserException;

@Data
public class ChangePasswdException extends UserException{
    private String loginName;
    private int code;
    private Object data;

    public ChangePasswdException(String loginName,Object data){
        super(loginName + "请修改密码");
        this.loginName = loginName;
        this.data = data;
        this.code = ErrorConstant.USER_100123;
    }

    public ChangePasswdException(String loginName){
        super(loginName + "请修改密码");
        this.loginName = loginName;
        this.code = ErrorConstant.USER_100123;
    }
}
