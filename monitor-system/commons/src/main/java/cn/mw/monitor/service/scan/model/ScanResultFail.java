package cn.mw.monitor.service.scan.model;

import lombok.Data;

import java.util.Date;

@Data
public class ScanResultFail {
    private Integer id;
    private Integer scanruleId;
    private String scanBatch;
    private String ipAddress;
    private String pollingEngine;
    private String monitorMode;
    private String cause;
    private Date scanTime;
    private String creator;
    private Date createDate;
    private String modifier;
    private Date modificationDate;
}
