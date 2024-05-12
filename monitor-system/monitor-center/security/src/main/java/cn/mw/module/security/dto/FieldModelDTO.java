package cn.mw.module.security.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统中已有topic字段的匹配模式
 * @author zah
 * @date 2023/04/14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldModelDTO {
    @ApiModelProperty("模式代码")
    private String modelCode;
    @ApiModelProperty("模式名称")
    private String modelName;
    @ApiModelProperty("用户匹配的模式")
    private String tagName;
    @ApiModelProperty("模式归属分类")
    private String modelType;
    @ApiModelProperty("告警字段id")
    private String fieldId;
    @ApiModelProperty("字段归属topic code")
    private String topicCode;
    @ApiModelProperty("告警等级")
    private String level;
    @ApiModelProperty("等级源字段")
    private String sourceField;
    @ApiModelProperty("等级映射值")
    private String mappValue;
}
