package cn.mw.monitor.service.zbx.model;

import lombok.Data;

@Data
public class Problem {
    private String id;
    private HostProblemType type;
    private String desc;
}
