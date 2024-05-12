package cn.mw.monitor.model.param.superfusion;

import lombok.Data;

/**
 * @author qzg
 * @date 2023/8/1
 */
@Data
public class SuperFusionBaseStorageParam {
    //存储id
    private String id;
    //磁盘类型
    private String type;
    //磁盘名称
    private String name;
    //剩余时间
    private String ssdLife;
    //磁盘总量
    private String storageTotal;
    //磁盘已使用
    private String storageUsed;
    //所属卷
    private String volumeName;
    //状态
    private String status;
    //宿主机名称
    private String hostName;
    //宿主机Id
    private String hostId;
    //磁盘标识
    private String diskSn;
    //折线图历史数据
    private Object data;
}
