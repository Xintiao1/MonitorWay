package cn.mw.monitor.service.scan.param;

import lombok.Data;

@Data
public class TopoPosNodeDebugParam {
    private static String UNKONW_MAC = "未知";

    private boolean virtualNode = false;

    private String assetName;

    public boolean hasChild = false;

    public String parentId = "";

    private Integer index;

    private String ip;

    private String mac = UNKONW_MAC;

    private String deviceName;

    private String deviceDesc;

    private boolean isRoot = false;

    private int level;

    private double x;

    private double y;


}
