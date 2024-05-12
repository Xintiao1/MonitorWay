package cn.joinhealth.monitor.zbx.model;

import lombok.Data;

@Data
public class ZbxGraph {
    private Long id;
    private String hostType;//主机类型 服务器、网络设备
    private String hostid;
    private String modelName;//模块名称（cpu使用率、内存使用率）
    private String itemName;//监控类型（内存）
    private String graphName;
    private String graphid;
}
