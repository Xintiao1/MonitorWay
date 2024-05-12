package cn.mw.monitor.activiti.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author lumingming
 * @createTime 2023814 21:50
 * @description
 */
@Data
public class Node {
    Boolean accept;
    String modelId;
    Boolean isComment;
    Integer moudleId;
    Integer instanceId;
    Integer customerId;

    @ApiModelProperty(value = "批注")
    String comment;
}
