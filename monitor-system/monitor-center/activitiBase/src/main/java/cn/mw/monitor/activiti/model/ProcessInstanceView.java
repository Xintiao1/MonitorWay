package cn.mw.monitor.activiti.model;

import cn.mw.monitor.activiti.dto.ProcessInstanceDTO;
import lombok.Data;

import java.util.List;

@Data
public class ProcessInstanceView {
    private int count;
    private List<ProcessInstanceDTO> data;
}
