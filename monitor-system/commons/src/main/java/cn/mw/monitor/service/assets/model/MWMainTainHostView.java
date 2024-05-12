package cn.mw.monitor.service.assets.model;

import cn.mw.monitor.service.assets.param.MWMainTainHostParam;
import lombok.Data;

@Data
public class MWMainTainHostView extends MWMainTainHostParam {
    private String inBandIp;

    private String instanceName;
}
