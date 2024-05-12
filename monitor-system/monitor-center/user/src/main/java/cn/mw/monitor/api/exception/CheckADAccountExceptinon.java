package cn.mw.monitor.api.exception;

import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.service.user.exception.UserException;
import lombok.Data;

/**
 * Created by zy.quaee on 2021/4/27 9:37.
 **/
@Data
public class CheckADAccountExceptinon extends UserException {
    private int code;
    public CheckADAccountExceptinon(){
        super("AD域服务器信息不正确");
        this.code = ErrorConstant.AD_100701;
    }
}
