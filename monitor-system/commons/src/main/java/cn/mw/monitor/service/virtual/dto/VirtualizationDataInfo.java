package cn.mw.monitor.service.virtual.dto;

import com.google.common.base.Strings;
import lombok.Data;

import java.util.Objects;

/**
 * @author qzg
 * @date 2022/8/30
 */
@Data
public class VirtualizationDataInfo {
    private String id;
    private String UUID;
    private String instanceName;
    private String status;
    private String state;
    private String PId;
    private String type;
    private String ip;
    private Integer monitorServerId;
    private String monitorServerName;
    private String hostId;
    private String hostName;
    private String clusterId;
    private String datacenterId;
    //基础数据
    private VirtualizationBaseInfo baseInfo;
    //监测数据
    private VirtualizationMonitorInfo monitorInfo;

    //运行时间
    private String upTime;

    public boolean isSame(VirtualizationDataInfo virtualizationDataInfo) {
        boolean ipChange = false;
        boolean pIdChange = false;
        if (!Strings.isNullOrEmpty(ip) && null != virtualizationDataInfo.getIp()) {
            ipChange = !ip.equals(virtualizationDataInfo.getIp());
        } else if (null == ip && null == virtualizationDataInfo.getIp()) {
            ipChange = false;
        } else {
            ipChange = true;
        }

        if (!Strings.isNullOrEmpty(PId) && !Strings.isNullOrEmpty(virtualizationDataInfo.getPId())) {
            pIdChange = !PId.equals(virtualizationDataInfo.getPId());
        } else {
            pIdChange = false;
        }

        boolean instanceNameChange = !instanceName.equals(virtualizationDataInfo.getInstanceName());

        return !(ipChange && pIdChange && instanceNameChange);
    }

}
