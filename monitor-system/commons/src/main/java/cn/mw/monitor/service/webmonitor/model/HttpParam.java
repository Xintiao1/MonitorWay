package cn.mw.monitor.service.webmonitor.model;

import lombok.Data;

/**
 * @author xhy
 * @date 2020/12/8 15:24
 */
@Data
public class HttpParam {
    private Integer id;
    private String httpId;
    private Integer monitorServerId;
}
