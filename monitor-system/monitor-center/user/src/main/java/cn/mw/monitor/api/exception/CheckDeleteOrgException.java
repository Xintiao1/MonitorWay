package cn.mw.monitor.api.exception;

import cn.mw.monitor.common.constant.ErrorConstant;
import lombok.Data;

@Data
public class CheckDeleteOrgException extends RuntimeException {

    private String msg;

    private String orgNames;

    private int code;

    public CheckDeleteOrgException(String msg, String orgNames) {
        super(msg + orgNames);
        this.code = ErrorConstant.ORG_100402;
        this.msg = msg;
        this.orgNames = orgNames;
    }

}
