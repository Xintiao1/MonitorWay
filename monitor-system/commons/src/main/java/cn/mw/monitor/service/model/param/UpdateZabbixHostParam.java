package cn.mw.monitor.service.model.param;

import lombok.Data;

/**
 * @author qzg
 * @date 2022/3/07 14:44
 */
@Data
public class UpdateZabbixHostParam {
    private Integer serverId;
    private String hostId;
    private String hostName;
}
