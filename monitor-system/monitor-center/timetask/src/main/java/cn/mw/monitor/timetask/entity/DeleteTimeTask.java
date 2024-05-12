package cn.mw.monitor.timetask.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author lumingming
 * @createTime 05 14:44
 * @description
 */
@Data
@ApiModel("删除规定定时")
public class DeleteTimeTask
{
    @ApiModelProperty("对象id")
    List<Integer> id;

    @ApiModelProperty("规定定时id")
    List<String> newTimeTaskid;
}
