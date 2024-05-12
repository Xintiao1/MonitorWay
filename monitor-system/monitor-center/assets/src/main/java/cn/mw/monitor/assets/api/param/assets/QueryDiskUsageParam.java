package cn.mw.monitor.assets.api.param.assets;

import lombok.Data;

import java.util.List;

@Data
public class QueryDiskUsageParam {
    private List<String> groupIds;
}
