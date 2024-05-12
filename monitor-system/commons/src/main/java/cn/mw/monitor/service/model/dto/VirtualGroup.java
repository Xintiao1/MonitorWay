package cn.mw.monitor.service.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class VirtualGroup {
    private InstanceNode root;
    private List<InstanceLine> lineList;
    private List<InstanceNode> nodes;
}
