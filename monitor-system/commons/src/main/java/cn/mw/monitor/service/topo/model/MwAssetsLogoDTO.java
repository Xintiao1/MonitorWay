package cn.mw.monitor.service.topo.model;

import lombok.Data;

@Data
public class MwAssetsLogoDTO {
    private int id;
    private int assetType;
    private String assetTypeName;
    private int assetSubType;
    private String assetSubTypeName;
    private String logoDesc;
    private String normalLogo;
    private String alertLogo;
    private String severityLogo;
    private String urgencyLogo;
}
