package cn.mw.monitor.screen.dto;

import lombok.Data;

@Data
public class TargetAssetsIdDto {
    //线路id
    private String linkId;
    //资产id
    private String id;

    private String linkTargetIp;

    //线路目标资产id
    private String targetAssetsId;

    //主机id
    private String linkHostId;
}
