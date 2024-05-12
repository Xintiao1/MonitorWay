package cn.mw.monitor.netflow.param;

import lombok.Data;

import java.util.List;

@Data
public class AssetParam {
    private Integer id;
    private String assetsName;
    private String assetId;
    private String ip;
    private List<InterfaceParam> interfaceParamList;
}
