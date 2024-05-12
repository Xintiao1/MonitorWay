package cn.mw.monitor.alert.param;

import lombok.Data;

/**
 * @author lbq
 * @date 2021/8/3
 */
@Data
public class NetworkLinkParam {
    private String portName;
    private String hostIp;
    private String assertId;
}
