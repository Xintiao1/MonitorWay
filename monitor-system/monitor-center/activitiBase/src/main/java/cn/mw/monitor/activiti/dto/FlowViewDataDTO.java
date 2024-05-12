package cn.mw.monitor.activiti.dto;

import lombok.Data;

import java.util.List;

/**
 * @author syt
 * @Date 2020/11/19 16:46
 * @Version 1.0
 */
@Data
public class FlowViewDataDTO {
    //节点数据
    private List<FlowNodeDTO> nodes;
    //连线数据
    private List<SequenceFlowDTO> edges;
}
