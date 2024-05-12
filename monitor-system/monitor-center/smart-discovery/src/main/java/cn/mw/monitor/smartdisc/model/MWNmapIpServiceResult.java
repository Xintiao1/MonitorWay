package cn.mw.monitor.smartdisc.model;

import lombok.Data;

import java.util.List;

@Data
public class MWNmapIpServiceResult {
    private MWNmapIpService mwNmapIpService;
    private List<MWNmapIpService> mwNmapIpServiceList;
}
