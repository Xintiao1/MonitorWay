package cn.mw.monitor.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ApiModel
@ToString
public class QueryInstanceParam {
    @ApiModelProperty("模型实例主键")
    private Integer modelInstanceId;
    @ApiModelProperty("模型实例名称")
    private String instanceName;
    //所属机房
    private String relationSiteRoom;

    //模型图标地址
    private String url;
    //是否内置图标
    private Integer customFlag;
}
