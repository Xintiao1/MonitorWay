package cn.mw.monitor.logManage.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "解析库最终结果添加类")
public class ParsingLibDataAddDTO {

    /**
     * 字段名：
     */
    @ApiModelProperty(value = "字段名")
    private String fieldName;

    /**
     * 字段值,varchar、int、datetime等
     */
    @ApiModelProperty(value = "字段值")
    private String fieldValue;

    /**
     * 后端暂时无用
     */
    @ApiModelProperty(value = "匹配数据处理规则")
    private String processRule;

    @ApiModelProperty(value = "匹配结果")
    private String matchResult = "OK";
}
