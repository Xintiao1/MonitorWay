package cn.mw.monitor.service.alert.dto;

import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.service.model.dto.DetailPageJumpDto;
import lombok.Data;

/**
 * @author xhy
 * @date 2020/4/21 13:08
 */
@Data
public class AssetsDto extends DetailPageJumpDto {
    private String id;
    private String assetsId;
    private String assetsName;
    private String assetsType;
    private String assetsIp;
    private Integer monitorServerId;
    private String monitorServerName;

    public void extractFrom(MwTangibleassetsDTO mwTangibleassetsDTO){
        this.id = mwTangibleassetsDTO.getId();
        this.assetsId = mwTangibleassetsDTO.getAssetsId();
        this.assetsName = mwTangibleassetsDTO.getAssetsName()==null?mwTangibleassetsDTO.getInstanceName():mwTangibleassetsDTO.getAssetsName();
        this.assetsType = mwTangibleassetsDTO.getAssetsTypeSubName();
        this.assetsIp = mwTangibleassetsDTO.getInBandIp();
        this.monitorServerId = mwTangibleassetsDTO.getMonitorServerId();
        this.monitorServerName = mwTangibleassetsDTO.getMonitorServerName();
    }
}
