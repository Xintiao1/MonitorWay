package cn.mw.monitor.model.dto;

import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date 2022/11/30
 */
@Data
public class MwModelViewTreeDTO {
    private String name;
    private String id;
    private String realId;
    private String pId;
    private Integer instanceNum;
    private String type;
    private Integer propertiesType;
    private List<Integer> modelGroupIdList;
    private String nodes;
    private String url;
    private Integer customFlag;

    private List<Integer> instanceIds;
}
