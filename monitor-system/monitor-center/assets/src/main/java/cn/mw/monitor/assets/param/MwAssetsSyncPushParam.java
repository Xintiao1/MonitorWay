package cn.mw.monitor.assets.param;

import lombok.Data;

import java.util.List;

@Data
public class MwAssetsSyncPushParam {
    private List<String> ids;
}
