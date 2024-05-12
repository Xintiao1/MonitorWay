package cn.mw.monitor.service.server.api.dto;

import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.util.SeverityUtils;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author gengjb
 * @description 资产详情DTO
 * @date 2024/1/12 8:44
 */
@Data
public class AssetsDetailDto {

    @ApiModelProperty("资产状态")
    private String id;

    @ApiModelProperty("资产状态")
    private String assetsStatus;

    @ApiModelProperty("资产类型")
    private String assetsTypeName;

    @ApiModelProperty("IP地址")
    private String ipAddress;

    @ApiModelProperty("运行时间")
    private String runTime;

    @ApiModelProperty("主机名称")
    private String hostName;

    @ApiModelProperty("规格型号")
    private String specifications;

    @ApiModelProperty("监控方式")
    private String monitorModeName;

    @ApiModelProperty("可用性")
    private String assetsUsability;

    @ApiModelProperty("描述")
    private String description;

    public void extractFrom(MwTangibleassetsTable mwTangibleassetsTable,ItemApplication itemApplication){
        this.id = mwTangibleassetsTable.getId();
        this.assetsStatus = mwTangibleassetsTable.getItemAssetsStatus();
        this.assetsTypeName = mwTangibleassetsTable.getAssetsTypeName();
        this.ipAddress = mwTangibleassetsTable.getInBandIp();
        this.hostName = mwTangibleassetsTable.getHostName();
        this.specifications = mwTangibleassetsTable.getSpecifications();
        this.monitorModeName = mwTangibleassetsTable.getMonitorModeName();
        this.description = mwTangibleassetsTable.getDescription();
        if(itemApplication != null){
            this.runTime = SeverityUtils.getLastTime(Long.parseLong(itemApplication.getLastvalue()));
        }
    }
}
