package cn.mw.monitor.api.exception;

import cn.mw.monitor.common.constant.ErrorConstant;
import lombok.Data;

/**
 * @description: 角色绑定用户 不能改变角色状态
 * @author:zy.quaee
 * @date:2020/12/29 9:55
 */
@Data
public class CheckChangeRoleException extends CheckRoleException {
    private String msg;

    private String roleNames;

    private int code;

    public CheckChangeRoleException(String msg, String roleNames) {
        super(msg + roleNames);
        this.msg = msg;
        this.roleNames = roleNames;
        this.code = ErrorConstant.ROLE_100312;
    }
}
