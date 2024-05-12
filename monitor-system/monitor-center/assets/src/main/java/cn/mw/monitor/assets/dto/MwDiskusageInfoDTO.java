package cn.mw.monitor.assets.dto;

import lombok.Data;

@Data
public class MwDiskusageInfoDTO {
    private String itemid;
    private String hostid;
    private String name;
    private String units;
    private String description;
    private String lastvalue;
}
