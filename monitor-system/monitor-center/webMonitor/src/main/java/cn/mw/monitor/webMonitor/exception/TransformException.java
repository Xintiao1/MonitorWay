package cn.mw.monitor.webMonitor.exception;

/**
 * @author syt
 * @Date 2021/7/16 9:47
 * @Version 1.0
 */
public class TransformException extends Exception{
    //默认构造器
    public TransformException() {
    }
    //带有详细信息的构造器，信息存储在message中
    public TransformException(String message) {
        super(message);
    }
}
