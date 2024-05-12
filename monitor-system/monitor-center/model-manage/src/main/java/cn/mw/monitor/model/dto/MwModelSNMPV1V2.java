package cn.mw.monitor.model.dto;

import cn.mw.monitor.service.assets.utils.RuleType;
import cn.mw.monitor.service.scan.model.SecurityProtocolType;
import lombok.Data;

@Data
public class MwModelSNMPV1V2 {
    private String port;
    private String version;
    //团体名
    private String community;

    private String assetsId;

}