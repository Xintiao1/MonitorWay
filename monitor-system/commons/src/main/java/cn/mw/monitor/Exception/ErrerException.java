package cn.mw.monitor.Exception;

/**
 * @author baochengbin
 * @date 2020/3/19
 */
public abstract class ErrerException extends RuntimeException{

    public ErrerException(String message){
        super(message);
    }

}
