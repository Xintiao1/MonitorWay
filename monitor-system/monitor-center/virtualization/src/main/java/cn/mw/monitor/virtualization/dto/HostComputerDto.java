package cn.mw.monitor.virtualization.dto;

import lombok.Data;

/**
 * @author syt
 * @Date 2020/7/8 9:35
 * @Version 1.0
 */
@Data
public class HostComputerDto {
//    值为带单位的结果
    private String CPUTotal;
    private String CPUUsed;
    private String CPUFree;

    private String memoryTotal;
    private String memoryUsed;
    private String memoryFree;

    private String storeTotal;
    private String storeUsed;
    private String storeFree;
}
