package cn.mw.monitor.visualized.dto;

import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.server.api.dto.ItemApplication;
import cn.mw.monitor.visualized.constant.RackZabbixItemConstant;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName
 * @Description 可视化流量信息DTO
 * @Author gengjb
 * @Date 2023/6/20 16:03
 * @Version 1.0
 **/
@Data
@ApiModel("可视化流量信息DTO")
public class MwVisualizedModuleLinkFlowDto {

    @ApiModelProperty("资产名称")
    private String assetsName;

    @ApiModelProperty("接口名称")
    private String interFaceName;

    @ApiModelProperty("流量出")
    private String flowOut;

    @ApiModelProperty("流量出单位")
    private String flowOutUnits;

    @ApiModelProperty("流量入")
    private String flowIn;

    @ApiModelProperty("流量入单位")
    private String flowInUnits;

    private Double sortValue;


    public void extractFrom(MwVisualizedCacheDto dto){
       this.assetsName = dto.getAssetsName();
       this.interFaceName = dto.getName() == null?dto.getItemName():dto.getName();
       if(dto.getItemName().contains(RackZabbixItemConstant.INTERFACE_IN_UTILIZATION)){
           this.flowIn = dto.getValue();
           this.flowInUnits = dto.getUnits();
       }
        if(dto.getItemName().contains(RackZabbixItemConstant.INTERFACE_OUT_UTILIZATION)){
            this.flowOut = dto.getValue();
            this.flowOutUnits = dto.getUnits();
        }
    }
}
