package cn.mw.monitor.api.exception;

import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.service.user.exception.UserException;
import lombok.Data;

/**
 * Created by zy.quaee on 2021/9/3 13:47.
 **/
@Data
public class CheckADConfigtExceptinon extends UserException {
    private int code;
    public CheckADConfigtExceptinon(){
        super("映射配置已存在!");
        this.code = ErrorConstant.AD_100711;
    }
}
