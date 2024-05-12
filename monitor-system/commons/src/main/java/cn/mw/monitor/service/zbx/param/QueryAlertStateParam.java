package cn.mw.monitor.service.zbx.param;

import cn.mw.monitor.bean.BaseParam;
import lombok.Data;

import java.util.List;

@Data
public class QueryAlertStateParam{

    private String instanceId;

    private String hostid;

    private Integer monitorServerId;

    private Boolean isAlert;

}
