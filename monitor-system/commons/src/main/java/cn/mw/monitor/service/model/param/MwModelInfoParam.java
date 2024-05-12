package cn.mw.monitor.service.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author xhy
 * @date 2021/2/25 9:26
 */
@ApiModel
@Data
public class MwModelInfoParam {
    private Integer modelId;
    private String modelName;
    private String modelIndex;
    private boolean isBase;
    private String nodes;
    private List<Integer> modelGroupIdList;
}
