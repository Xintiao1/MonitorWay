package cn.mw.monitor.activiti.model;

import cn.mw.monitor.activiti.param.ProcessNode;
import cn.mw.monitor.activiti.param.WorkFlowDef;
import lombok.Data;

import java.util.List;

@Data
public class ProcessView {
    private String processData;
    private List<Integer> users;
    private List<Integer> orgs;
    private List<Integer> groups;
}
