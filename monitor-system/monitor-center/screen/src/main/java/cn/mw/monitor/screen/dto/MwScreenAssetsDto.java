package cn.mw.monitor.screen.dto;

import cn.mw.monitor.service.assets.model.AssetTypeIconDTO;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Map;

/**
 * @ClassName
 * @Description 首页资产DTO
 * @Author gengjb
 * @Date 2023/5/29 9:29
 * @Version 1.0
 **/
@Data
@ApiModel("首页资产DTO")
public class MwScreenAssetsDto {

    @ApiModelProperty("服务器ID")
    private Integer serverId;

    @ApiModelProperty("主机ID")
    private String assetsId;

    @ApiModelProperty("监控状态")
    private Boolean monitorFlag;

    @ApiModelProperty("类型名称")
    private String typeName;

    @ApiModelProperty("资产ID")
    private String id;

    @ApiModelProperty("类型图标地址")
    private String url;

    @ApiModelProperty("是否启用了模型")
    private boolean isModel;

    @ApiModelProperty("资产状态")
    private String assetsStatus;


    public void extractFrom(MwTangibleassetsTable mwTangibleassetsTable, Map<Integer , AssetTypeIconDTO> typeIconDTOMap,boolean isModel){
        this.serverId = mwTangibleassetsTable.getMonitorServerId();
        this.assetsId = mwTangibleassetsTable.getAssetsId();
        this.monitorFlag = mwTangibleassetsTable.getMonitorFlag();
        this.typeName = mwTangibleassetsTable.getAssetsTypeName();
        this.id = mwTangibleassetsTable.getId() == null?String.valueOf(mwTangibleassetsTable.getModelInstanceId()):mwTangibleassetsTable.getId();
        this.isModel = isModel;
        this.assetsStatus = mwTangibleassetsTable.getItemAssetsStatus();
        AssetTypeIconDTO iconDTO = typeIconDTOMap.get(mwTangibleassetsTable.getAssetsTypeId());
        if(null != iconDTO){
            this.url = iconDTO.getUrl();
        }
    }
}
