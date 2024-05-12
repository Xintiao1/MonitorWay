package cn.mw.monitor.netflow.param;

import lombok.Data;

@Data
public class InterfaceParam {
    private Integer id;
    private Integer ifIndex;
    private String ifName;
}
