package cn.mw.monitor.service.scan.dto;

import lombok.Data;

@Data
public class MwTopoGraphSnmpv1v2DTO {
    private Integer id;
    private String port;
    private String community;
    private String topoGraphId;
}
