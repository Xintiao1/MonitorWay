package cn.mw.monitor.smartdisc.model;

import lombok.Data;

import java.util.List;

@Data
public class MWNmapIpServiceList extends MWNmapIpService{

    private List<MWNmapIpService> ipChilds;

}
