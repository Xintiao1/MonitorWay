package cn.mw.monitor.report.param;

import lombok.Data;


@Data
public class ReportMessageFromParam {
    private String reportId;
    private String ruleId;
    private String emailServerAddress;
    private String emailServerPort;
    private String emailSendUsername;
    private String emailSendPassword;
    private Boolean isSsl;
    private Boolean isSmtp;
    private String personal;
}
