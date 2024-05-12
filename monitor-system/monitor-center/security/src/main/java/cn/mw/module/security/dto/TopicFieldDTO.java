package cn.mw.module.security.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * kafka数据源中topic的字段映射
 * @author zah
 * @date 2023/04/14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicFieldDTO {
    @ApiModelProperty("字段id")
    private String id;
    @ApiModelProperty("字段代码")
    private String fieldCode;
    @ApiModelProperty("字段名称")
    private String fieldName;
    @ApiModelProperty("是否有固定匹配模式")
    private boolean isMatchModel;
    @ApiModelProperty("匹配模式码表")
    private String matchModelTableName;
    @ApiModelProperty("匹配模式输入方式")
    private String inputType;
    @ApiModelProperty("匹配模式列表")
    private List<FieldModelDTO> fieldModel;
    @ApiModelProperty("归属topic代码")
    private String topicCode;
    @ApiModelProperty("归属topic名称")
    private String topicName;
    @ApiModelProperty("映射kafka字段")
    private String kafkaField;
    @ApiModelProperty("映射规则")
    private Object mappingRule;
    @ApiModelProperty("映射规则名称")
    private String mappingRuleName;
    @ApiModelProperty("归属kafka数据源Id")
    private String kafkaId;
    @ApiModelProperty("删除标志")
    private Boolean isDelete;
    @ApiModelProperty("消费规则")
    private Boolean consumeRule;
    @ApiModelProperty("前端需要字段")
    private String tagShow;
}
