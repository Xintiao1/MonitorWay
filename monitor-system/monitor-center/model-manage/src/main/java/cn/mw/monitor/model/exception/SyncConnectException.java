package cn.mw.monitor.model.exception;


/**
 * @author qzg
 * @date 2023/12/25
 */
public class SyncConnectException extends  RuntimeException {
    private String message;

    public SyncConnectException(String message) {
        super(message);
        this.message = message;
    }
}
