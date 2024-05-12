package cn.mw.monitor.service.user.exception;

import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.service.user.exception.UserException;
import lombok.Data;

/**
 * Created by zy.quaee on 2021/6/23 11:16.
 **/
@Data
public class PasswdExpireException extends UserException {
    private String loginName;
    private int code;

    public PasswdExpireException(String loginName){
        super(loginName + ErrorConstant.USER_MSG_100141);
        this.loginName = loginName;
        this.code = ErrorConstant.USER_100141;
    }

}
