package cn.mw.monitor.service.ipmanage.exception;

public class IpScanInterruptException extends RuntimeException {
    public IpScanInterruptException(){
        super("ip扫描终止");
    }
}
