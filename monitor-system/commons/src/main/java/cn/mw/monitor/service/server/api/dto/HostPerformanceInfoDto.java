package cn.mw.monitor.service.server.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author syt
 * @Date 2020/7/14 15:38
 * @Version 1.0
 */
@Data
public class HostPerformanceInfoDto {
    //zabbix hostid
    private String hostId;
//    cpu利用率
    private String cpuUnitilization;
//    内存利用率
    private String memoryUtilization;
//    延时(对应zabbix itemName为ICMP_RESPONSE_TIME的数据)
    private String delayed;

    @ApiModelProperty("接口性能数据")
    private List<HostIfPerformanceInfo> ifPerformanceInfoList = new ArrayList<>();

    //    不带单位的cpu利用率
    private double cpuUTIL;
    //    不带单位的cpu利用率
    private double memoryUTIL;

    public void addHostIfPerformanceInfo(HostIfPerformanceInfo hostIfPerformanceInfo){
        this.ifPerformanceInfoList.add(hostIfPerformanceInfo);
    }
}
