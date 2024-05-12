package cn.mw.monitor.model.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RelationGroupDTO {
    private String label;
    private String id;
    private Integer realGroupId;
    private boolean defautGroupFlag;
}
