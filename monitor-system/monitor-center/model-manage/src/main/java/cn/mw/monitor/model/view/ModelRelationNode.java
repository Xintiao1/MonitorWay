package cn.mw.monitor.model.view;

import lombok.Data;

@Data
public class ModelRelationNode {
    //前端显示id
    private String id;

    //实际模型id
    private Integer realModelId;

    //模型图标
    private String img;

    //本端模型名称
    private String label;

}
