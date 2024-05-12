package cn.mw.monitor.service.topo.model;

import cn.mw.monitor.service.topo.param.TopoGroupAddParam;
import lombok.Data;

@Data
public class TopoGroupDTO {
    private String id;
    private String parentId;
    private String name;
    private String path;
    private Integer pathId;
    /**
     * 描述
     */
    private String desc;

    public void extractFrom(TopoGroupAddParam topoGroupAddParam){
        this.name = topoGroupAddParam.getName();
        this.parentId = topoGroupAddParam.getParentId();
    }
}
