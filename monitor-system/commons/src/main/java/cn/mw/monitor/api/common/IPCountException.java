package cn.mw.monitor.api.common;

public class IPCountException extends RuntimeException {
    private int maxCount;

    public IPCountException(int maxCount){
        super("超过最大ip扫描数:" + maxCount);
        this.maxCount = maxCount;
    }
}
