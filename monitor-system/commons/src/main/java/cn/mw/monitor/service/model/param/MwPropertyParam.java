package cn.mw.monitor.service.model.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author qzg
 * @date 2023/4/16
 */
@Data
public class MwPropertyParam {
    @ApiModelProperty("es存的索引id")
    private String indexId;
    @ApiModelProperty("属性名称")
    private String propertiesName;
    @ApiModelProperty("属性类型id)")
    private Integer propertiesTypeId;
    @ApiModelProperty("模型属性值信息")
    private Object propertiesValue;
    //是否精准查询
    private boolean filterQuery;
}
