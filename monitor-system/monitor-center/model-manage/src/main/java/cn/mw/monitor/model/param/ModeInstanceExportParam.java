package cn.mw.monitor.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


/**
 * @author qzg
 * @date 2021/12/06
 */
@Data
@ApiModel
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModeInstanceExportParam {

    @ApiModelProperty("资产的主键Id")
    private String tangibleId;
    @ApiModelProperty("模型的索引")
    private String modelIndex;
    @ApiModelProperty("模型的索引的Id")
    private String modelIndexId;
    @ApiModelProperty("模型的Id")
    private Integer modelId;
    @ApiModelProperty("模型的名称")
    private String modelName;
    @ApiModelProperty("模型的描述")
    private String modelDesc;
    @ApiModelProperty("模型的实例Id")
    private Integer modelInstanceId;
    @ApiModelProperty("模型的实例名称")
    private String modelInstanceName;
    @ApiModelProperty("模型属性类型1字符串,2整形数字,3浮点型数据,4布尔型,5日期类型,6结构体，7:Ip地址")
    private Integer propertiesType;
    @ApiModelProperty("模型属性Id")
    private Integer propertiesId;
    @ApiModelProperty("模型属性IndexId")//存入es数据库的35位uuid   mw_f424a45a236e4fbf8ce709abbc6837a5
    private String propertiesIndexId;
    @ApiModelProperty("模型属性的名称")
    private String propertiesName;
    @ApiModelProperty("模型属性的值")
    private Object propertiesValue;

}
