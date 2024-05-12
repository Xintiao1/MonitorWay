package cn.mw.monitor.api.exception;

public class CheckInsertNmapTaskException extends RuntimeException {
    private String msg;

    private int code;

    public CheckInsertNmapTaskException(int code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

}
