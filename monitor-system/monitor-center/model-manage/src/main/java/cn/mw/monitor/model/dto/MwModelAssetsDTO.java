package cn.mw.monitor.model.dto;

import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author syt
 * @Version 1.0
 */
@Data
public class MwModelAssetsDTO {
//    资产id
    @ApiModelProperty("主键ID")
    private String id;
//    资产名称
    @ApiModelProperty("资产名称")
    private String assetsName;
//    监控服务器id
    @ApiModelProperty("监控服务器ID")
    private Integer monitorServerId;
//    主机id
    @ApiModelProperty("主机ID")
    private String assetsId;

    private String interFaceName;

    @ApiModelProperty("IP地址")
    private String assetsIp;

    public void extractFrom(MwTangibleassetsDTO mwTangibleassetsDTO){
        this.id = mwTangibleassetsDTO.getId();
        this.assetsId = mwTangibleassetsDTO.getAssetsId();
        this.assetsName = mwTangibleassetsDTO.getAssetsName()==null?mwTangibleassetsDTO.getHostName():mwTangibleassetsDTO.getAssetsName();
        this.assetsIp = mwTangibleassetsDTO.getInBandIp();
        this.monitorServerId = mwTangibleassetsDTO.getMonitorServerId();
    }

}
