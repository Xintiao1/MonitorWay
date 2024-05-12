package cn.mw.monitor.engineManage.api.exception;

import lombok.Data;

@Data
public abstract class EngineException extends RuntimeException{
    public EngineException(String message){
        super(message);
    }

    public abstract int getCode();
}
