package cn.mw.monitor.model.param;

import lombok.Data;

@Data
public class AddAndUpdateInstanceTopoInfoParam {
    private String id;
    private int instanceId;
    private long instanceViewId;
    private String topoInfo;
}
