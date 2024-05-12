package cn.mw.monitor.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author xhy
 * @date 2021/2/8 12:11
 */
@Data
@ApiModel
public class GetRelationDataParam {
    private String relationKey;
    private List dataVal;
}
