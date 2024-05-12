package cn.mw.monitor.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author qzg
 * @date 2023/8/31
 */
@Data
@ApiModel
public class MwModelJudgeDTO {
    @ApiModelProperty("id")
    private Integer id;
    @ApiModelProperty("评价时间")
    private Date judgeTime;
    @ApiModelProperty("评价内容")
    private String judgeMessage;
    @ApiModelProperty("评价分值")
    private Integer judgeScore;
    @ApiModelProperty("评价人Id")
    private String userId;
    @ApiModelProperty("评价人")
    private String userName;
    @ApiModelProperty("评价人")
    private Integer instanceId;
}
