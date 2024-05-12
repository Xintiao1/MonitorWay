package cn.mw.monitor.assets.dto;

import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import lombok.Data;

/**
 * @author syt
 * @Version 1.0
 */
@Data
public class AssetsDTO {
//    资产id
    private String id;
//    资产名称
    private String assetsName;
//    监控服务器id
    private Integer monitorServerId;
//    主机id
    private String assetsId;

    private String interFaceName;

    public void extractFrom(MwTangibleassetsTable table){
       this.id = table.getId()==null?String.valueOf(table.getModelInstanceId()):table.getId();
       this.assetsName = table.getAssetsName()==null?table.getInstanceName():table.getAssetsName();
       this.monitorServerId = table.getMonitorServerId();
       this.assetsId = table.getAssetsId();
    }
}
