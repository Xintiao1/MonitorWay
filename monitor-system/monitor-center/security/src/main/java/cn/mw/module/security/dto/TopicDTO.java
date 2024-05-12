package cn.mw.module.security.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * kafka数据源中的topic
 * @author zah
 * @date 2023/04/14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicDTO {
    @ApiModelProperty("topic代码")
    private String topicCode;
    @ApiModelProperty("topic名称")
    private String topicName;
    @ApiModelProperty("消费规则")
    private Boolean consumeRule;
    @ApiModelProperty("topic字段集")
    private List<TopicFieldDTO> fieldList;
}
