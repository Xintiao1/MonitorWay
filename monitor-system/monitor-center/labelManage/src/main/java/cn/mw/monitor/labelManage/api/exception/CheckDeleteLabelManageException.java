package cn.mw.monitor.labelManage.api.exception;

import cn.mw.monitor.common.constant.ErrorConstant;
import lombok.Data;

@Data
public class CheckDeleteLabelManageException extends RuntimeException  {

    private String msg;

    private String labelNames;

    private int code;

    public CheckDeleteLabelManageException(String msg, String labelNames) {
        super(msg + labelNames);
        this.code = ErrorConstant.LABELMANAGECODE_220107;
        this.msg = msg;
        this.labelNames = labelNames;
    }

}
