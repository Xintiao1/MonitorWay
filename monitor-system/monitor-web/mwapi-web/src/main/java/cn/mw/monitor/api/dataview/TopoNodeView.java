package cn.mw.monitor.api.dataview;

import cn.mw.zbx.common.ZbxConstants;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class TopoNodeView {
    private  String assetName;
    private String ip;
    private String cpuUsage;
    private String memUsage;
    private String delay;
    private String position;
    private  String assetId;
    public TopoNodeView(){
        this.assetName = "未知";
        this.ip = "未知";
        this.cpuUsage = ZbxConstants.NO_DATA;
        this.memUsage = ZbxConstants.NO_DATA;
        this.delay = "未知";
        this.position = "未知";
    }
}
