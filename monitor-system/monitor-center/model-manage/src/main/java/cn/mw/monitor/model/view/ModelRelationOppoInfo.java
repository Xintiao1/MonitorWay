package cn.mw.monitor.model.view;

import lombok.Data;

//对端模型信息
@Data
public class ModelRelationOppoInfo {
    //对端映射数量
    private Integer num;

    //本端关系名称
    private String name;

    //对端模型id
    private Integer value;
}
