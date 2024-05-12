package cn.mw.monitor.api.exception;

import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.service.user.exception.UserException;
import lombok.Data;

/**
 * Created by zy.quaee on 2021/4/28 9:03.
 **/
@Data
public class CheckADConnectException extends UserException {
    private int code;
    public CheckADConnectException(){
        super("AD域连接失败");
        this.code = ErrorConstant.AD_100701;
    }
}
