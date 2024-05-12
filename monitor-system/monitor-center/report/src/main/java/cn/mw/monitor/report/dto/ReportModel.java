package cn.mw.monitor.report.dto;

import lombok.Data;

/**
 * @author xhy
 * @date 2020/12/21 14:34
 */
public enum  ReportModel {
    ReportModel(1,""),
    ;

    private Integer reportId;
    private String reportName;

    ReportModel(Integer reportId,String reportName) {
        this.reportName = reportName;
        this.reportId = reportId;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public Integer getReportId() {
        return reportId;
    }

    public void setReportId(Integer reportId) {
        this.reportId = reportId;
    }
}
