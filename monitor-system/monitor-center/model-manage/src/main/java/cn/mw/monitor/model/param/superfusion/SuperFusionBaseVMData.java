package cn.mw.monitor.model.param.superfusion;

import lombok.Data;

/**
 * 虚拟机基础数据
 * @author qzg
 * @date 2023/8/2
 */
@Data
public class SuperFusionBaseVMData {
    private String ip;
    private String name;
    //主机名称
    private String hostName;
    //持续运行时间
    private String uptime;
    //系统版本
    private String osVersion;
    //硬件类型
    private String hardWareType;

    private String cpuRatio;
    //核数
    private Integer cpus;
    //主频
    private String mhz;

    private String memRatio;
    private String memTotal;
    private String memFree;

    //配置总容量
    private String diskTotal;
    //已配置容量
    private String diskFree;
    private String diskRatio;


    private Object data;


}
