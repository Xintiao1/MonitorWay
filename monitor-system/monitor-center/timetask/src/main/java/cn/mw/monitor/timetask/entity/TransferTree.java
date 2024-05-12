package cn.mw.monitor.timetask.entity;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
@ApiModel("中转模块树图")
public class TransferTree {
    //主键
    @ApiModelProperty("模块ID")
    private Integer modelId;

}
