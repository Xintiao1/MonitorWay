package cn.mw.monitor.service.assets.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class IpAssetsNameDTO {
    private String assetId;
    private String inBandIp;
    private String outBandIp;
    private String assetsName;
}
