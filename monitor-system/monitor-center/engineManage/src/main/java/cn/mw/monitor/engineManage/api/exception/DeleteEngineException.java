package cn.mw.monitor.engineManage.api.exception;

import cn.mw.monitor.common.constant.ErrorConstant;
import lombok.Data;

@Data
public class DeleteEngineException extends EngineException{

    private String msg;
    private String engineNames;
    private int code;

    public DeleteEngineException(String msg, String engineNames){
        super(msg+engineNames);
        this.msg=msg;
        this.engineNames = engineNames;
        this.code = ErrorConstant.ENGINEMANAGECODE_240105;
    }
}
