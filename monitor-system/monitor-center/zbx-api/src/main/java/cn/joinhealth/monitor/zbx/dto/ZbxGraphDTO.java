package cn.joinhealth.monitor.zbx.dto;

import lombok.Data;

@Data
public class ZbxGraphDTO {
    private Long id;
    private String hostType;
    private String hostid;
    private String modelName;
    private String itemName;
    private String graphName;
    private String graphid;
}
