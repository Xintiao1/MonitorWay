package cn.mw.monitor.assets.dto;

import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.server.api.dto.ItemApplication;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author gengjb
 * @description 资产导出指标DTO
 * @date 2023/12/20 10:06
 */
@Data
@ApiModel("资产导出指标DTO")
public class AssetsExportIndexDto {

    @ApiModelProperty("资产主键")
    @ExcelProperty(value = {"资产主键"})
    private String assetsId;

    @ApiModelProperty("服务器ID")
    @ExcelProperty(value = {"服务器ID"})
    private Integer serverId;

    @ApiModelProperty("主机ID")
    @ExcelProperty(value = {"主机ID"})
    private String hostId;

    @ApiModelProperty("资产名称")
    @ExcelProperty(value = {"资产名称"})
    private String assetsName;

    @ApiModelProperty("IP地址")
    @ExcelProperty(value = {"IP地址"})
    private String ipAddress;

    @ApiModelProperty("监控项名称")
    @ExcelProperty(value = {"监控项名称"})
    private String itemName;

    public void extractFrom(ItemApplication itemApplication, MwTangibleassetsTable tangibleassetsTable){
        this.assetsId = tangibleassetsTable.getId();
        this.serverId = tangibleassetsTable.getMonitorServerId();
        this.hostId = itemApplication.getHostid();
        this.assetsName = tangibleassetsTable.getAssetsName()==null?tangibleassetsTable.getInstanceName():tangibleassetsTable.getAssetsName();
        this.ipAddress = tangibleassetsTable.getInBandIp();
        this.itemName = itemApplication.getName();
    }

}
