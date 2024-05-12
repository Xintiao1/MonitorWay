package cn.mw.monitor.model.param;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author qzg
 * @date 2022/4/24
 */
@Data
@ApiModel
public class QueryModelGangedFieldParam {
    private Integer modelId;
    //联动字段值
    private String fieldVal;
    //属性字段id
    private String fieldIndexId;
}
