package cn.mw.monitor.model.dto;

import lombok.Data;

import java.util.Date;

@Data
public class ModelRelationGroupDTO {
    private Integer id;
    private Integer ownModelId;
    private String relationGroupName;
    private String relationGroupDesc;
    private String creator;
    private Date createDate;
    private String modifier;
    private Date modificationDate;
    private Boolean deleteFlag;
    private Boolean defautGroupFlag;
}
