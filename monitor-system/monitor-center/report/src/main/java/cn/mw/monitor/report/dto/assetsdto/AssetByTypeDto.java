package cn.mw.monitor.report.dto.assetsdto;

import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.server.api.dto.ItemApplication;
import lombok.Data;

import java.util.Date;

@Data
public class AssetByTypeDto {
    private String id;
    private String assetsId;
    private String assetsName;
    private String inBandIp;
    private Integer assetsTypeId;
    private String typeName;
    private Integer monitorServerId;
    private Date createDate;
    private String itemAssetsStatus;


    public void extractFrom(MwTangibleassetsTable tangibleassetsTable){
       this.id = tangibleassetsTable.getId() == null?String.valueOf(tangibleassetsTable.getModelInstanceId()):tangibleassetsTable.getId();
       this.assetsId = tangibleassetsTable.getAssetsId();
       this.assetsName = tangibleassetsTable.getAssetsName() == null?tangibleassetsTable.getInstanceName():tangibleassetsTable.getAssetsName();
       this.inBandIp = tangibleassetsTable.getInBandIp();
       this.assetsTypeId = tangibleassetsTable.getAssetsTypeId();
       this.typeName = tangibleassetsTable.getAssetsTypeName();
       this.monitorServerId = tangibleassetsTable.getMonitorServerId();
       this.createDate = tangibleassetsTable.getCreateDate();
    }
}
