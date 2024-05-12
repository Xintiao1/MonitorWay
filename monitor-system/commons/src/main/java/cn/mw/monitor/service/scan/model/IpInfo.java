package cn.mw.monitor.service.scan.model;

import cn.mw.monitor.service.scan.dto.IPInfoDTO;
import lombok.Data;

import java.util.Objects;

@Data
public class IpInfo {
    private String mac;
    private String deviceName;
    private String portName;


    public void extractFrom(IPInfoDTO ipInfoDTO){
        this.mac = ipInfoDTO.getMac();
        this.deviceName = ipInfoDTO.getLinkDeviceName();
        this.portName = ipInfoDTO.getPortName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IpInfo ipInfo = (IpInfo) o;
        return Objects.equals(mac, ipInfo.mac);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mac);
    }
}
