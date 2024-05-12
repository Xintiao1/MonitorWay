package cn.mw.monitor.api.exception;

import cn.mw.monitor.common.constant.ErrorConstant;
import lombok.Data;

@Data
public class CheckDeleteRoleException extends CheckRoleException {

    private String msg;

    private String roleNames;

    private int code;

    public CheckDeleteRoleException(String msg, String roleNames) {
        super(msg + roleNames);
        this.msg = msg;
        this.roleNames = roleNames;
        this.code = ErrorConstant.ROLE_100311;
    }

}
