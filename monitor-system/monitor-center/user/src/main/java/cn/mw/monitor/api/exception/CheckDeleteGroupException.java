package cn.mw.monitor.api.exception;

import cn.mw.monitor.common.constant.ErrorConstant;
import lombok.Data;

@Data
public class CheckDeleteGroupException extends RuntimeException {

    private String msg;

    private String groupNames;

    private int code;

    public CheckDeleteGroupException(String msg, String groupNames) {
        super(msg + groupNames);
        this.code = ErrorConstant.USERGROUPCODE_250105;
        this.msg = msg;
        this.groupNames = groupNames;
    }

}
