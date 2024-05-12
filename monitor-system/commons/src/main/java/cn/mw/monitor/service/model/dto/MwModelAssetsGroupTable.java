package cn.mw.monitor.service.model.dto;

import lombok.Data;

@Data
public class MwModelAssetsGroupTable {
    private Integer id;
    private String groupId;
    private Integer monitorServerId;
    //模型分组id（相当于之前的资产类型id）
    private Integer assetsSubtypeId;
}
