package cn.mw.monitor.service.scan.dto;

import lombok.Data;

@Data
public class MwTopoGraphSnmpv3DTO {
    private Integer id;
    private String port;
    private String secName;
    private String contextName;
    private String secLevel;
    private String authAlg;
    private String authValue;
    private String privAlg;
    private String priValue;
    private String topoGraphId;
}
