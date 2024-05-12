package cn.mw.monitor.service.smartDiscovery.param;

import cn.mw.monitor.bean.BaseParam;
import lombok.Data;

@Data
public class QueryNmapResultParam extends BaseParam {

    private Integer taskId;
    private String taskName;
}
