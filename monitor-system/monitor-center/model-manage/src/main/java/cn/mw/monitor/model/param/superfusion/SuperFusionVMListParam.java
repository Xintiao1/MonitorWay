package cn.mw.monitor.model.param.superfusion;

import lombok.Data;

/**
 * @author qzg
 * @date 2023/8/1
 */
@Data
public class SuperFusionVMListParam {
    //CPU利用率 0.03 -> 3%
    private String cpuRatio;
    // 0.03
    private Double cpuVal;
    private String id;
    private String ip;
    private String name;
    //磁盘利用率
    private String memRatio;
    private Double memVal;
    //磁盘利用率
    private String ioRatio;
    private Double ioVal;
    private String status;
    private String vmId;
    //宿主机名称
    private String hostName;
    //宿主机Id
    private String host;

    public void setVmId(String vmId){
        this.vmId = vmId;
        this.id = vmId;
    }

}
