package cn.mw.monitor.api.exception;

import lombok.Data;

/**
 * @author baochengbin
 * @date 2020/3/31
 */
@Data
public abstract class CheckRoleException extends RuntimeException{
    public CheckRoleException(String message){
        super(message);
    }

    public abstract int getCode();
}
