package cn.mw.monitor.api.exception;

import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.service.user.exception.UserException;
import lombok.Data;

/**
 * @description:TODO
 * @author:zy.quaee
 * @date:2020/12/18 15:00
 */
@Data
public class PasswdCheckException extends UserException {
    private int code;

    public PasswdCheckException(String msg){
        super(msg);
        this.code = ErrorConstant.USER_100137;
    }
}
