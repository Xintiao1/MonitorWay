package cn.mw.monitor.service.model.param;

import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import lombok.Data;

/**
 * @ClassName BaseDetailParam
 * @Description ToDo
 * @Author gengjb
 * @Date 2023/2/12 15:02
 * @Version 1.0
 **/
@Data
public class BaseDetailParam {

    private String id;
    private String assetsId;
    private String assetsType;
    private String assetsIp;
    private Integer monitorServerId;
    private String monitorServerName;

    public void extractFrom(MwTangibleassetsDTO mwTangibleassetsDTO){
        this.id = mwTangibleassetsDTO.getId();
        this.assetsId = mwTangibleassetsDTO.getAssetsId();
        this.assetsType = mwTangibleassetsDTO.getAssetsTypeSubName();
        this.assetsIp = mwTangibleassetsDTO.getInBandIp();
        this.monitorServerId = mwTangibleassetsDTO.getMonitorServerId();
        this.monitorServerName = mwTangibleassetsDTO.getMonitorServerName();
    }
}
