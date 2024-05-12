package cn.mw.monitor.model.param.superfusion;

import lombok.Data;

/**
 * @author qzg
 * @date 2023/8/1
 */
@Data
public class SuperFusionHostListParam {
    //CPU占比 0.03 -> 3%
    private String cpuRatio;
    // 0.03
    private Double cpuVal;
    private String id;
    private String ip;
    private String name;
    //是否主控设备
    private String master;
    //物理内存占比
    private String memRatio;
    private Double memVal;
    //配置内存占比
    private String confRatio;
    private Double confVal;
    private Integer status;

}
