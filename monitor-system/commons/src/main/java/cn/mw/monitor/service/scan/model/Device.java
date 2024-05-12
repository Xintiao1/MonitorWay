package cn.mw.monitor.service.scan.model;

import lombok.Data;

import java.util.List;

@Data
public class Device {
    String ip;
    List<InterfaceInfo> interfaceInfoList;

    @Override
    public String toString() {
        return "Device{" +
                "ip='" + ip + '\'' +
                '}';
    }
}
