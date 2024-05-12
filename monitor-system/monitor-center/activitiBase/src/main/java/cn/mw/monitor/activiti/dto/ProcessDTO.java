package cn.mw.monitor.activiti.dto;

import cn.mw.monitor.activiti.model.ProcessParamView;
import lombok.Builder;
import lombok.Data;

import java.util.List;


@Data
@Builder
public class ProcessDTO {
    private String processName;
    private String processDefinitionId;
    private boolean processStatus;
    private ProcessParamView processData;
    private String modelName;
    private String modelId;
    private int action;

    private String modelLastId;
    private List<String> modelLastName;
    private List<Integer> user;
    private List<String> userText;
    private List<Integer> org;
    private List<String> orgText;
    private List<Integer> group;
    private List<String> groupText;
}
