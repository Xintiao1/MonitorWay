package cn.mw.monitor.assets.dto;

import cn.mw.monitor.bean.BaseParam;
import lombok.Data;

/**
 * @author qzg
 * @Version 1.0
 */
@Data
public class AssetsSyncDelayParam extends BaseParam {
    //资产id
    private String id;
    //资产名称
    private String assetsName;
    //监控服务器id
    private Integer monitorServerId;
    //主机id
    private String assetsId;

    private String hostId;

    private String applicationName;

}
