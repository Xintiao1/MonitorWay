package cn.mw.monitor.activiti.model;

import cn.mw.monitor.activiti.entiy.NodeProcess;
import cn.mw.monitor.activiti.param.ProcessNode;
import cn.mw.monitor.service.activiti.param.BaseProcessParam;
import lombok.Data;

import java.util.List;

@Data
public class ProcessParamView extends BaseProcessParam {
    private String processId;
    private String processName;
    private String processDesc;
    private ProcessNode childNode;
    private List<Integer> org;
    private List<Integer> group;
    private List<Integer> user;
    private boolean principalcheckbox;
    private boolean orgIdscheckbox;
    private boolean groupIdscheckbox;
    private List<NodeProcess> nodeProcesses;
    //流程显示变量
    private String ccSelfSelectFlag;
    private String directorLevel;
    private String examineEndDirectorLevel;
    private String examineMode;
    private String noHanderAction;
    private String priorityLevel;
    private String selectMode;
    private String selectRange;
    private String settype;
    private int type;
    private boolean error;


}
