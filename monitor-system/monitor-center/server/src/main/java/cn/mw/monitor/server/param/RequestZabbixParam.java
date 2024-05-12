package cn.mw.monitor.server.param;

import lombok.Data;

/**
 * @author qzg
 * @date 2021/8/17
 */
@Data
public class RequestZabbixParam {
    private String itemid;
    private String name;
    private String lastvalue;
    private String units;
    private String valueType;
    private String valuemapid;

}
