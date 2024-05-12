package cn.mw.monitor.service.user.exception;

import cn.mw.monitor.common.constant.ErrorConstant;
import lombok.Data;

@Data
public class UserLockedException extends UserException{

    private String loginName;
    private Integer retrySec;
    private int code;

    public UserLockedException(String loginName,Integer retrySec){
        super(loginName + "已经锁定,请等待"+retrySec+"秒后重试!");
        this.loginName = loginName;
        this.retrySec = retrySec;
        this.code = ErrorConstant.USER_100124;
    }
    public UserLockedException(){
        super("已经锁定!");
        this.code = ErrorConstant.USER_100124;
    }
    public UserLockedException(String loginName){
        super(loginName + "密码过期，已经锁定!");
        this.loginName = loginName;
        this.code = ErrorConstant.USER_100124;
    }
}
