package cn.mw.monitor.util.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Size;

/**
 * @author lumingming
 * @createTime 20230412 10:39
 * @description
 */
@Data
@ApiModel(value = "账户管理数据")
public class Progress {
    /**
     * 进度
     */
    @ApiModelProperty("进度条")
    private Double percentage;

    /**
     * 进度
     */
    @ApiModelProperty("是否能执行")
    private Boolean isOver = true;


    @ApiModelProperty("引擎id")
    private String instanceId = "0";

    public void init(){
        percentage=Double.valueOf("0");
        isOver=true;
        instanceId = "0";
    }

}
