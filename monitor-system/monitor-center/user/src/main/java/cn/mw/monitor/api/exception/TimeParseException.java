package cn.mw.monitor.api.exception;

import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.service.user.exception.UserException;
import lombok.Data;

/**
 * @description:
 * @author:zy.quaee
 * @date:2020/12/28 11:31
 */
@Data
public class TimeParseException extends UserException {
    private int code;

    public TimeParseException(){
        super("用户登录控制时间格式输入错误！");
        this.code = ErrorConstant.USER_100210;
    }
}
