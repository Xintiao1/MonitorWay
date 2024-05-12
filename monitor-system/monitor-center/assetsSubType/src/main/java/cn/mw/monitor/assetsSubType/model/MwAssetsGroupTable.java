package cn.mw.monitor.assetsSubType.model;

import lombok.Data;

@Data
public class MwAssetsGroupTable {
    private Integer id;
    private String groupId;
    private Integer monitorServerId;
    private Integer assetsSubtypeId;
}
