package cn.mw.monitor.service.assets.model;

import lombok.Data;

@Data
public class IpAssetsCount {
    private String id;
    private String ip;
    private String assetsName;
    private int count;
}
