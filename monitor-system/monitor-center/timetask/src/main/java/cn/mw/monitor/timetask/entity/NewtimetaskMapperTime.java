package cn.mw.monitor.timetask.entity;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author lumingming
 * @createTime 03 14:36
 * @description
 */
@Data
@ApiModel("绑定时间计划")
public class NewtimetaskMapperTime extends BaseParam implements Serializable {
    private static final long serialVersionUID = 1L;
    @ApiModelProperty("计划组id")
    private Integer id ;
    @ApiModelProperty("模块id")
    private Integer modelId ;
    @ApiModelProperty("当前时间计划Id（添加不传，编辑传）")
    private String newtimetaskId ;

}
