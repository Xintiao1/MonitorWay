package cn.mw.monitor.service.link.dto;

import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.link.param.AssetsParam;
import cn.mw.monitor.service.server.api.dto.ItemApplication;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author gengjb
 * @description 线路接口信息DTO
 * @date 2023/9/11 14:34
 */
@Data
@ApiModel("线路接口信息DTO")
public class MwLinkInterfaceDto {

    @ApiModelProperty("线路ID")
    private String linkId;

    @ApiModelProperty("线路名称")
    private String linkName;

    @ApiModelProperty("服务器ID")
    private Integer serverId;

    @ApiModelProperty("主机ID")
    private String hostId;

    @ApiModelProperty("接口名称")
    private String interfaceName;

    @ApiModelProperty("上行带宽")
    private String upBnadWithUtilization;

    @ApiModelProperty("下行带宽")
    private String downBnadWithUtilization;


    public void extractFrom(AssetsParam assetsParam,String interfaceName){
        this.hostId = assetsParam.getAssetsId();
        this.serverId = assetsParam.getMonitorServerId();
        this.interfaceName = interfaceName;
    }

}
