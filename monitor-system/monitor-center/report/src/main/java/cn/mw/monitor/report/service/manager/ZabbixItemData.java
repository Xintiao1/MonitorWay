package cn.mw.monitor.report.service.manager;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ZabbixItemData {
    private String itemId;
    private String itemName;
}
