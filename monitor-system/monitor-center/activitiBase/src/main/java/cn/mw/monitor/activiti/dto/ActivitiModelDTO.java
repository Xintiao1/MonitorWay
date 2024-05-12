package cn.mw.monitor.activiti.dto;

import lombok.Data;

/**
 * @author syt
 * @Date 2020/9/21 11:15
 * @Version 1.0
 */
@Data
public class ActivitiModelDTO {
    private String id;
    private String name;
    private String des;
    private String key;
    private Integer version;

    public ActivitiModelDTO(String id, String name, String des, String key, Integer version) {
        this.id = id;
        this.name = name;
        this.des = des;
        this.key = key;
        this.version = version;
    }

    public ActivitiModelDTO() {
    }
}
