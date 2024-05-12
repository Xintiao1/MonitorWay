package cn.mw.monitor.webMonitor.api.param.webMonitor;

import cn.mw.monitor.service.webmonitor.model.HttpParam;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author baochengbin
 * @date 2020/3/16
 */
@Data
public class DeleteWebMonitorParam {
    List<Integer>  idList = new ArrayList<>();

    List<HttpParam> httpTestIds = new ArrayList<>();

}

