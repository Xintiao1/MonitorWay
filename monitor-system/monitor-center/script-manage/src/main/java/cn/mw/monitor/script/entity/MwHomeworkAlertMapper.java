package cn.mw.monitor.script.entity;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

;

/**
 * @author lumingming
 * @createTime 2023502 10:56
 * @description
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "告警对应方案结果")
public class MwHomeworkAlertMapper extends BaseParam {

    @ApiModelProperty("主键ID")
    private Integer id;

    /**
     * 告警触发的名称
     */
    @ApiModelProperty("告警")
    private Integer homeworkAlertId;

    /**
     * 告警标题
     */
    @ApiModelProperty("告警标题")
    private Integer versionId;

}
