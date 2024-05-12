package cn.mw.monitor.smartdisc.model;

import lombok.Data;

@Data
public class MWNmapLiveNodeGroup {

    private Integer id;
    private String liveNodeName;
    private String liveNodeGroup;
    private Boolean deleteFlag;
}
