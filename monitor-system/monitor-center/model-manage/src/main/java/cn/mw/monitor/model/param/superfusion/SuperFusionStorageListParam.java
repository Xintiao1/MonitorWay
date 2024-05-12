package cn.mw.monitor.model.param.superfusion;

import lombok.Data;

/**
 * @author qzg
 * @date 2023/8/1
 */
@Data
public class SuperFusionStorageListParam {
    //存储id
    private String id;
    private String type;
    private String name;
    //容量使用率
    private String storageRatio;
    private Double storageVal;
    //所属卷
    private String volumeName;

    private String status;
    //宿主机名称
    private String hostName;
    //宿主机Id
    private String hostId;
    //io读速率
    private String ioReadRate;
    //io写速率
    private String ioWriteRate;
    //io读速率
    private long ioReadVal;
    //io写速率
    private long ioWriteVal;
    //io延时
    private String ioAwait;


}
