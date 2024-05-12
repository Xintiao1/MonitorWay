package cn.mw.monitor.graph.modelAsset;

import lombok.Data;

@Data
public class ModelRelationInfo {
    //模型Id
    private int modelId;

    //允许关联数
    private String num;

    //关系名称
    private String relationName;

    //关系id
    private String id;
}
