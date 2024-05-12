package cn.mw.monitor.api.dataview;

import cn.mw.monitor.service.server.api.dto.HostIfPerformanceInfo;
import lombok.Data;

@Data
public class IfView {
    private String ifName;
    private String in;
    private String out;
    private String pickLoss;
    private String sendLoss;

    public void extractData(HostIfPerformanceInfo hostIfPerformanceInfo){
        this.in = hostIfPerformanceInfo.getInBps();
        this.out = hostIfPerformanceInfo.getOutBps();
        this.pickLoss = hostIfPerformanceInfo.getPickDropped();
        this.sendLoss = hostIfPerformanceInfo.getSendDropped();
    }
}
