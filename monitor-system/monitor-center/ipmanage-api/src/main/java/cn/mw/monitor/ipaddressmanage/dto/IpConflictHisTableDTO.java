package cn.mw.monitor.ipaddressmanage.dto;

import cn.mw.monitor.service.scan.dto.IPInfoDTO;
import cn.mw.monitor.service.scan.model.IpInfo;
import lombok.Data;

import java.util.Date;

@Data
public class IpConflictHisTableDTO {
    private String id;
    private String conflictId;
    private String ip;
    private String mac;
    private String deviceName;
    private String portName;
    private Date createTime;

    public void extractFrom(IPInfoDTO ipInfoDTO){
        this.ip = ipInfoDTO.getIp();
        this.mac = ipInfoDTO.getMac();
        this.deviceName = ipInfoDTO.getLinkDeviceName();
        this.portName = ipInfoDTO.getPortName();
    }

    public void extractFrom(String ip ,IpInfo ipInfo){
        this.ip = ip;
        this.mac = ipInfo.getMac();
        this.deviceName = ipInfo.getDeviceName();
        this.portName = ipInfo.getPortName();
    }
}
