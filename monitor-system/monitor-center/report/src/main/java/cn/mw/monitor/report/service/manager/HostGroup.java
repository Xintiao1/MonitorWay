package cn.mw.monitor.report.service.manager;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class HostGroup {
    private List<String> hostIds = new ArrayList<>();;
    private List<ZabbixItemData> itemDatas = new ArrayList<>();
}
