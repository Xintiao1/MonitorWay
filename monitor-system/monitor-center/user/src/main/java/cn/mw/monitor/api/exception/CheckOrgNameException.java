package cn.mw.monitor.api.exception;

import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.service.user.exception.UserException;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by zy.quaee on 2021/6/17 9:46.
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class CheckOrgNameException extends UserException {
    private String msg;

    private String orgNames;

    private int code;

    public CheckOrgNameException(String msg) {
        super(msg);
        this.code = ErrorConstant.ORG_100418;
        this.msg = msg;
    }
}
