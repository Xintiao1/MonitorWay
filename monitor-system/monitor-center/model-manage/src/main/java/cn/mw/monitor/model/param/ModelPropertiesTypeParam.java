package cn.mw.monitor.model.param;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author xhy
 * @date 2021/2/7 9:25
 */
@Data
@ApiModel
public class ModelPropertiesTypeParam {
    private String propertyIndex;
    private Integer propertyType;
}
