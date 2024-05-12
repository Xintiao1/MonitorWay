package cn.mw.monitor.service.assets.param;

import lombok.Data;

@Data
public class DeleteTangAssetsID implements Comparable<DeleteTangAssetsID>{
    private String id;
    private String assetsId;
    private int monitorServerId;
    private String pollingEngine;
    //当监控方式为虚拟化时，创建的虚拟化资产自动发现的那些虚拟机，主机太多删除不掉时需要做的处理
    private Integer monitorMode;
    //资产类型ID
    private Integer assetsTypeId;
    //模型id
    private String modelId;

    @Override
    public int compareTo(DeleteTangAssetsID o) {
        if(this.monitorServerId > o.getMonitorServerId()){
            return 1;
        }else if (this.monitorServerId < o.getMonitorServerId()){
            return -1;
        }
        return this.assetsId.compareTo(o.getAssetsId());
    }
}
