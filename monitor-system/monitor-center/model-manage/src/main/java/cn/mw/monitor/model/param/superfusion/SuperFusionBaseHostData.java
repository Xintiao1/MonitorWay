package cn.mw.monitor.model.param.superfusion;

import lombok.Data;

/**
 * 主机基础数据
 * @author qzg
 * @date 2023/8/2
 */
@Data
public class SuperFusionBaseHostData {
    private String ip;
    private String name;
    //持续运行时间
    private String uptime;
    private Integer vmNum;
    //系统版本
    private String osVersion;
    //硬件类型
    private String hardWareType;

    private String cpuRatio;
    //核数
    private Integer cores;
    //线程
    private Integer cpuThreads;
    private Integer sockets;
    //CPU类型
    private String cpuType;

    private String memRatio;
    private String memTotal;
    private String memFree;
    //计算内存容量
    private String baseTotalByte;
    //配置总容量
    private String confTotalByte;
    //已配置容量
    private String confUsedByte;
    private String confRatio;


}
