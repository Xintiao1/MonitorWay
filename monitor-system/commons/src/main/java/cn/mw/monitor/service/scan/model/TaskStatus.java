package cn.mw.monitor.service.scan.model;

import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class TaskStatus {
    private String topoId;
    private String topoName;
    private AtomicBoolean terminalScanEnable = new AtomicBoolean(false);
    private AtomicBoolean netFlowTaskEnable = new AtomicBoolean(false);
    private Date netFlowTaskLastExecuteTime;

    private Date topoInfoTaskLastExecuteTime;

    private AtomicBoolean alertUnfirmTaskEnable = new AtomicBoolean(false);
    private Date alertUnfirmExecuteTime;

    public String getTopoId() {
        return topoId;
    }

    public void setTopoId(String topoId) {
        this.topoId = topoId;
    }

    public String getTopoName() {
        return topoName;
    }

    public void setTopoName(String topoName) {
        this.topoName = topoName;
    }

    public AtomicBoolean getTerminalScanEnable() {
        return terminalScanEnable;
    }

    public void setTerminalScanEnable(AtomicBoolean terminalScanEnable) {
        this.terminalScanEnable = terminalScanEnable;
    }

    public Date getNetFlowTaskLastExecuteTime() {
        return netFlowTaskLastExecuteTime;
    }

    public void setNetFlowTaskLastExecuteTime(Date netFlowTaskLastExecuteTime) {
        this.netFlowTaskLastExecuteTime = netFlowTaskLastExecuteTime;
    }

    public Date getTopoInfoTaskLastExecuteTime() {
        return topoInfoTaskLastExecuteTime;
    }

    public void setTopoInfoTaskLastExecuteTime(Date topoInfoTaskLastExecuteTime) {
        this.topoInfoTaskLastExecuteTime = topoInfoTaskLastExecuteTime;
    }

    public AtomicBoolean getNetFlowTaskEnable() {
        return netFlowTaskEnable;
    }

    public Date getAlertUnfirmExecuteTime() {
        return alertUnfirmExecuteTime;
    }

    public void setAlertUnfirmExecuteTime(Date alertUnfirmExecuteTime) {
        this.alertUnfirmExecuteTime = alertUnfirmExecuteTime;
    }

    public AtomicBoolean getAlertUnfirmTaskEnable() {
        return alertUnfirmTaskEnable;
    }
}
