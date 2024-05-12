package cn.mw.monitor.service.virtual.dto;

import lombok.Data;

/**
 * @author qzg
 * 虚拟化设备基础数据
 * @date 2022/9/15
 */
@Data
public class VirtualizationBaseInfo {
    private String instanceName;
    //数据中心数量
    private Integer dataCenterNum;
    //主机数量
    private Integer hostNum;
    //虚拟机数量
    private Integer vmNum;
    //集群数量
    private Integer clusterNum;
    //网络数量
    private Integer netNum;
    //数据存储数量
    private Integer dataStoreNum;
    //ip地址
    private String ip;
    //厂商
    private String vendor;
    //型号
    private String model;
    //运行时间
    private String upTime;
    //状态
    private String status;
    //版本
    private String version;
    //全称
    private String fullName;
    //虚拟机CPU核数
    private Integer cpuNum;
}
