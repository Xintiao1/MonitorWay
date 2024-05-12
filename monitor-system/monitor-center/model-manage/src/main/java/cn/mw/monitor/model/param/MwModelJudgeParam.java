package cn.mw.monitor.model.param;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author qzg
 * @date 2023/8/31
 */
@Data
@ApiModel
public class MwModelJudgeParam extends BaseParam {
    @ApiModelProperty("评价内容")
    private String judgeMessage;
    @ApiModelProperty("评价分值")
    private String judgeScore;
    @ApiModelProperty("查询分值类型")
    private String queryScoreType;
    @ApiModelProperty("评价人Id")
    private Integer userId;
    @ApiModelProperty("评价人")
    private String userName;
    private String instanceId;
    @ApiModelProperty("评价周期0:仅一次，1:一年,2:无限制")
    private Integer judgeCycle;




}
