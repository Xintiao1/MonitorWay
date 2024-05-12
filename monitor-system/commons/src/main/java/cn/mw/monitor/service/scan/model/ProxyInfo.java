package cn.mw.monitor.service.scan.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Data
public class ProxyInfo {
    private static final List<String> sepList = new ArrayList<>();

    private String[] ips;
    private Integer port;

    static {
        sepList.add(";");
        sepList.add(",");
    }

    public ProxyInfo(String ips ,Integer port){
        for(String sep : sepList){
            if(ips.indexOf(sep) >= 0){
                this.ips = ips.split(sep);
                break;
            }
        }

        if(null == this.ips){
            this.ips = new String[1];
            this.ips[0] = ips;
        }
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProxyInfo proxyInfo = (ProxyInfo) o;
        return Arrays.equals(ips, proxyInfo.ips);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(ips);
    }
}
