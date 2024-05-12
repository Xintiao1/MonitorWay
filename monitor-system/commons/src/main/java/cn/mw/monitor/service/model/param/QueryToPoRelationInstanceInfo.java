package cn.mw.monitor.service.model.param;

import lombok.Data;
import org.neo4j.ogm.session.Session;

import java.util.Set;

@Data
public class QueryToPoRelationInstanceInfo {
    //查询的指定空间
    private String space;
    //查询深度
    private String deep;
    //本体模型Id
    private String ownModelId;
    //本体实例Id
    private String ownInstanceId;
    //本体模型Index
    private String ownModelIndex;
    //关联的模型Id
    private String relationModelId;
}
