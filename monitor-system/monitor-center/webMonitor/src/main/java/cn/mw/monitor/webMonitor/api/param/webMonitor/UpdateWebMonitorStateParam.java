package cn.mw.monitor.webMonitor.api.param.webMonitor;

import lombok.Data;

/**
 * @author baochengbin
 * @date 2020/4/25
 */
@Data
public class UpdateWebMonitorStateParam {

    private Integer id;

    private String httpTestId;
    private String enable;
    private Integer monitorServerId;
}
