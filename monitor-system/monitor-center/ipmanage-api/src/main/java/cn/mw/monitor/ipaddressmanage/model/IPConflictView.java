package cn.mw.monitor.ipaddressmanage.model;

import cn.mw.monitor.service.scan.dto.IPInfoDTO;
import cn.mw.monitor.service.scan.model.IpInfo;
import lombok.Data;

import java.util.List;

@Data
public class IPConflictView {
    private String ip;
    private String mac1;
    private String deviceName1;
    private String portName1;
    private String mac2;
    private String deviceName2;
    private String portName2;

    public void extractFrom(IPInfoDTO ipInfoDTO){
        this.ip = ipInfoDTO.getIp();
        this.mac1 = ipInfoDTO.getMac();
        this.deviceName1 = ipInfoDTO.getLinkDeviceName();
        this.portName1 = ipInfoDTO.getPortName();

        List<IpInfo> ipInfoList = ipInfoDTO.getConflictsIp();
        if(null != ipInfoList && ipInfoList.size() > 0){
            IpInfo ipInfo = ipInfoList.get(0);
            this.mac2 = ipInfo.getMac();
            this.deviceName2 = ipInfo.getDeviceName();
            this.portName2 = ipInfo.getPortName();
        }

    }
}
