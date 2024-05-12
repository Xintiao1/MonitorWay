package cn.mw.monitor.assets.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MonitorServer {
    private Integer monitorServerId;
    private List<String> zabbixIds = new ArrayList<String>();
}
