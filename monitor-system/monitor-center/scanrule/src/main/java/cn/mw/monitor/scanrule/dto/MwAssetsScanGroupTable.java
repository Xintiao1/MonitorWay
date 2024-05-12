package cn.mw.monitor.scanrule.dto;

import lombok.Data;

/**
 * @ClassName MwAssetsScanGroupTable
 * @Description ToDo
 * @Author gengjb
 * @Date 2022/9/22 16:32
 * @Version 1.0
 **/
@Data
public class MwAssetsScanGroupTable {
    private Integer id;
    private String groupId;
    private Integer monitorServerId;
    private Integer assetsSubtypeId;
}
