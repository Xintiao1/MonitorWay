package cn.mw.monitor.service.scan.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class TopoDiscoverying {
    private String groupId;
    private String id;
    private String topoName;
    private String topoDesc;
    private String topoGraph;
    private String topoType;
    private String creator;
    private Date createDate;
    private TopoStatus topoStatus;

    public TopoDiscoverying(){

    }
}
