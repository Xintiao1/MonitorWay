package cn.mw.monitor.timetask.entity;


import cn.mw.monitor.bean.BaseParam;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author lumingming
 * @createTime 24 16:52
 * @description
 */
@Data
@ApiModel("定时任务动作")
public class MwNcmTimetaskTimePlan extends BaseParam implements Serializable  {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("tree_id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id ;
    @ApiModelProperty("任务名称")
    private String timeName ;
    @ApiModelProperty("任务计划")
    private String timeCronChinese ;
    @ApiModelProperty("任务函数")
    private String timeCron ;
    @ApiModelProperty("H.小时 W.周 M.月 S.自定义 任务类型")
    private String timeType ;
    @ApiModelProperty("对应选项所选择的值")
    private String timeChoice ;
    @ApiModelProperty("对应选项所选择的值")
    private String timeHms ;

}
