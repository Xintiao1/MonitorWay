package cn.mw.monitor.service.model.param;

import lombok.Data;

/**
 * @author qzg
 * @date 2022/12/2
 */
@Data
public class MwSyncZabbixAssetsParam {
    private String instanceName;
    private Integer monitorServerId;
    private String hostId;
}
