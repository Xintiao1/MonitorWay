package cn.mw.monitor.model.exception;


/**
 * @author xhy
 * @date 2021/2/25 9:46
 */
public class ModelManagerException extends  RuntimeException {
    private String message;

    public ModelManagerException(String message) {
        super(message);
        this.message = message;
    }
}
