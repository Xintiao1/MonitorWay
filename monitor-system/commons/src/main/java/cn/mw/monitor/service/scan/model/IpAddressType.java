package cn.mw.monitor.service.scan.model;

public enum IpAddressType {
    unknown(0) ,ipv4(4) ,ipv6(16);

    private int code;

    IpAddressType(int code){
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static IpAddressType fromValue(int code) {
        for (IpAddressType s : IpAddressType.values()) {
            if (s.getCode() == code) {
                return s;
            }
        }
        return null;
    }

}
