package cn.mw.monitor.service.user.exception;

import lombok.Data;

@Data
public abstract class UserException extends RuntimeException{
    public UserException(String message){
        super(message);
    }

    public abstract int getCode();
}
