package cn.mw.monitor.timetask.entity;


import cn.mw.monitor.bean.BaseParam;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;


@Data
@ApiModel("中转模块")
public class Transfer extends BaseParam {
    //主键
    @ApiModelProperty("发送：动作ID")
    private Integer actionId;
    @ApiModelProperty("发送：模块ID")
    private Integer modelId;
    @ApiModelProperty("发送：模块ID")
    private List<Integer> id;

    @ApiModelProperty("当前时间计划Id（添加不传，编辑传）")
    private String newtimetaskId ;
    @ApiModelProperty("查询")
    private String search ;
    @ApiModelProperty("返回：模块ID")
    private PageInfo tree;
    @ApiModelProperty("返回：是否存在保留数据")
    private boolean treeHave;
}
