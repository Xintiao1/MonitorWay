package cn.mw.monitor.logManage.exception;

/**
 * 日志管理自定义异常类
 */
public class LogManageBusinessException extends RuntimeException {
    public LogManageBusinessException() {
        super();
    }

    public LogManageBusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public LogManageBusinessException(String message) {
        super(message);
    }

    public LogManageBusinessException(Throwable cause) {
        super(cause);
    }
}
