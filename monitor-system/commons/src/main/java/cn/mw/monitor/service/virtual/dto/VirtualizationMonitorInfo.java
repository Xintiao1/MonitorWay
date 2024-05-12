package cn.mw.monitor.service.virtual.dto;

import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * 虚拟化设备监控数据
 * @date 2022/9/15
 */
@Data
public class VirtualizationMonitorInfo {
    private String instanceName;
    private Integer instanceId;
    private String virtualType;
    //操作系统
    private String operatingSystem;
    //ip地址
    private String ip;
    private List<VirtualizationIpAdressInfo> ipParam;

    //厂商
    private String vendor;
    //型号
    private String model;
    //运行时间
    private String upTime;
    //状态
    private String status;
    //已用CPU
    private Integer sortUsageCPU;
    //总CPU
    private Integer sortTotalCPU;
    //可用CPU
    private Integer sortFreeCPU;
    //已用CPU 带单位
    private String usageCPU;
    //总CPU 带单位
    private String totalCPU;
    //可用CPU 带单位
    private String freeCPU;
    //CPU利用率
    private String cpuUtilization;

    //CPU利用率排序
    private Double sortCpuUtilization;
    //已用内存
    private Long sortUsageMemory;
    //已用内存 带单位
    private String usageMemory;
    //总内存
    private Long sortTotalMemory;
    //总内存 带单位
    private String totalMemory;
    //可用内存
    private Long sortFreeMemory;
    //可用内存  带单位
    private String freeMemory;
    //内存利用率
    private String memoryUtilization;
    //内存利用率排序
    private Double sortMemoryUtilization;
    //已用存储
    private Long sortUsageStorage;
    //总存储
    private Long sortTotalStorage;
    //可用空间
    private Long sortFreeStorage;
    //已用空间 带单位
    private String usageStorage;
    //置备空间 带单位
    private String totalStorage;
    //可用存储 带单位
    private String freeStorage;
    //存储利用率
    private String storageUtilization;

    //存储利用率排序字段
    private Double sortStorageUtilization;

    //是否纳管
    private Boolean isConnect;
    private String id;
    private String UUID;
    private String pId;
    private String type;

    public String toDebugString() {
        return "VirtualizationMonitorInfo{" +
                "instanceName='" + instanceName + '\'' +
                ", instanceId=" + instanceId +
                ", virtualType='" + virtualType + '\'' +
                ", ip='" + ip + '\'' +
                ", vendor='" + vendor + '\'' +
                ", model='" + model + '\'' +
                ", upTime='" + upTime + '\'' +
                ", status='" + status + '\'' +
                ", isConnect=" + isConnect +
                ", id='" + id + '\'' +
                ", UUID='" + UUID + '\'' +
                ", pId='" + pId + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
