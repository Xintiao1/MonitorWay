package cn.mw.monitor.assets.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @ClassName MwAssetsCustomFieldDto
 * @Description ToDo
 * @Author gengjb
 * @Date 2022/7/5 10:34
 * @Version 1.0
 **/
@Data
public class MwAssetsCustomFieldDto {

    @ApiModelProperty("字段ID")
    private int colId;

    @ApiModelProperty("字段名称")
    private String prop;

    @ApiModelProperty("字段监控项名称")
    private String label;

    @ApiModelProperty("字段是否显示")
    private boolean visible;

    @ApiModelProperty("字段ID集合")
    private List<Integer> ids;
    
    @ApiModelProperty("字段类型，1：监控项字段  2：标签字段")
    private int type;

    @ApiModelProperty("字段排序")
    private int orderNumber;

}
