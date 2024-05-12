package cn.mw.monitor.screen.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssetOrgDto {
    private String OrgName;
    private String itemAssetStatus;
    private String assetName;
    private String assetId;
    private String assetsTypeName;
    private Integer orgId;
    private Integer assetsTypeId;

}
