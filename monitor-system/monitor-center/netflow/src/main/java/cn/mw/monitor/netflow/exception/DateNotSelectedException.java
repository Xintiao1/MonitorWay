package cn.mw.monitor.netflow.exception;

import cn.mw.monitor.Exception.ErrerException;

/**
 * @author gui.quanwang
 * @className DateNotSelectedException
 * @description 时间项未选择异常
 * @date 2023/3/16
 */
public class DateNotSelectedException extends ErrerException {

    public DateNotSelectedException(String message) {
        super(message);
    }

}
