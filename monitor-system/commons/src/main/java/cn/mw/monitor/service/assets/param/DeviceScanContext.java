package cn.mw.monitor.service.assets.param;

import cn.mw.monitor.service.scan.model.ProxyInfo;
import lombok.Data;

@Data
public class DeviceScanContext {
    private ProxyInfo proxyInfo;
    private AddUpdateTangAssetsParam addUpdateTangAssetsParam;
}
