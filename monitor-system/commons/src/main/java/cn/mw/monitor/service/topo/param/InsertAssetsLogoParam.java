package cn.mw.monitor.service.topo.param;

import lombok.Data;

@Data
public class InsertAssetsLogoParam {
    private int id;
    private int assetType;
    private int assetSubType;
    private String logoName;
    private String logoDesc;
    private String normalLogo;
    private String alertLogo;
    private String severityLogo;
    private String urgencyLogo;
}
