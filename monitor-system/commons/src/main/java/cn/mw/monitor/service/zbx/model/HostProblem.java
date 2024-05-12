package cn.mw.monitor.service.zbx.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class HostProblem {
    private Integer monitorServerId;
    private String hostName;
    private String hostId;
    private String host;
    private List<Problem> problemList;

    public void addProblem(Problem problem){
        if(null == problemList){
            problemList = new ArrayList<>();
        }

        problemList.add(problem);
    }
}
