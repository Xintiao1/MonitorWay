package cn.mw.monitor.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author qzg
 * @date 2021/12/06
 */
@Data
@ApiModel
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelExportDataInfoParam {
    @ApiModelProperty("表头数据")
    private String tableName;

    @ApiModelProperty("匹配的属性indexId")
    private String propertiesIndexId;

    @ApiModelProperty("模型Id")
    private Integer modelId;

    @ApiModelProperty("模型IndexId")
    private String modelIndexId;

    @ApiModelProperty("匹配的属性类型")
    private String propertiesType;

    private Boolean isOnly;

    private Boolean isMust;

    private Boolean ignore;

    private String dropOp;

    private String type;

    private Integer modelView;

    private String relationModelIndex;

    private Boolean isMatchKey;

    private Boolean isImportEditor;
}
