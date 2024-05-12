package cn.mw.monitor.service.virtual.dto;

import lombok.Data;

/**
 * 虚拟机参数
 * @author qzg
 * @date 2022/8/24
 */
@Data
public class VirtualMachineDTO {
    private String vmId;
    private String name;
    private String status;
    private String state;
    private Double provisionedSpace;
    private Double usedSpace;
    private Integer hostCPU;
    private Integer hostMemory;
    private String hostId;
    private String pId;
    private String vmIp;
    private String vmDNS;
}
