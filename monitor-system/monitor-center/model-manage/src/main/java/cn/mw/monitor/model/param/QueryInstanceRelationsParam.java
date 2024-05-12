package cn.mw.monitor.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author xhy
 * @date 2021/2/25 15:02
 */
@ApiModel
@Data
public class QueryInstanceRelationsParam {
    private Integer modelId;
    private List<Integer> instanceIds;
    private String modelName;
    private Integer ownRelationNum;//左-本端实例关联数量（1：一对一，2：一对多）
    private Integer oppositeRelationNum;//右-对端实例关联数量（1：一对一，2：一对多）

    private Integer charId;
    private Integer instanceId;
    private Integer type;
}
