package cn.mw.monitor.service.topo.model;

import cn.mwpaas.common.utils.StringUtils;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TopoGroupView {
    public static final String PATH_SEP = "/";
    private String id;
    private String parentId;
    private String type;
    private String label;
    private String path;
    private List<TopoGroupPathView> pathName = new ArrayList<>();
    private List<Integer> respPerson;
    private List<List<Integer>> orgIds;
    private List<Integer> groupIds;
    private boolean isFinish;
    private String scanType;

    private List<TopoGroupView> children = new ArrayList<>();

    /**
     * 描述
     */
    private String desc;

    public void addTopoComponet(TopoGroupView topoGroupView){
        children.add(topoGroupView);
    }

    public void addPath(TopoGroupPathView topoGroupPathView){
        pathName.add(topoGroupPathView);
    }

    public void addPathAll(List<TopoGroupPathView> list){
        pathName.addAll(list);
    }

    public void extractFrom(GroupInfo groupInfo){
        TopoGroupDTO topoGroupDTO = groupInfo.getTopoGroupDTO();
        this.id = topoGroupDTO.getId();
        this.parentId = topoGroupDTO.getParentId();
        this.label = topoGroupDTO.getName();
        this.type = groupInfo.getType().name();
        this.path = topoGroupDTO.getPath();
        this.isFinish = groupInfo.isFinished();
        this.desc = groupInfo.getTopoGroupDTO().getDesc();
        if(StringUtils.isNotEmpty(groupInfo.getTopoType())){
            this.scanType = groupInfo.getTopoType();
        }
    }
}
