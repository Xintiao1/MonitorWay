package cn.mw.monitor.activiti.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProcessDefDTO {
    private int id;
    private String processDefinitionId;
    private String newProcessDefinitionId;
    private String processData;
    private String processInstanceKey;
    private List<Integer> userIds = new ArrayList<>();
    private List<Integer> organizes = new ArrayList<>();
    private List<Integer> groupIds = new ArrayList<>();
    private Integer status;
    private Integer version;
}
