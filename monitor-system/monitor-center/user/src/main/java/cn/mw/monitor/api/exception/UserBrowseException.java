package cn.mw.monitor.api.exception;

import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.service.user.exception.UserException;
import lombok.Data;

/**
 * Created by zy.quaee on 2021/4/29 23:51.
 **/
@Data
public class UserBrowseException extends UserException {
    private int code;
    public UserBrowseException(){
        super("用户查询失败");
        this.code = ErrorConstant.USER_100105;
    }
}
