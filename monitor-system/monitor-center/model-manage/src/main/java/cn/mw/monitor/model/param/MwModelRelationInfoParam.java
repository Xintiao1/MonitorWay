package cn.mw.monitor.model.param;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author qzg
 * @date 2023/4/20 17:15
 */
@Data
@ApiModel
public class MwModelRelationInfoParam {
    private Integer modelId;
    private Integer modelInstanceId;
    private String instanceName;
    private String relationModelSystem;
}
