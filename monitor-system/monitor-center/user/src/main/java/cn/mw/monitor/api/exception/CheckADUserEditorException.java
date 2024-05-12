package cn.mw.monitor.api.exception;

import lombok.Data;

/**
 * Created by zy.quaee on 2021/5/11 9:43.
 **/
@Data
public class CheckADUserEditorException extends RuntimeException {

    private String msg;

    private int code;

    public CheckADUserEditorException(int code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

}