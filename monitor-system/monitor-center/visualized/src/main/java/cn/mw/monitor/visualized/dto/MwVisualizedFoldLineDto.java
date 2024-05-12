package cn.mw.monitor.visualized.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @ClassName
 * @Description 折线图返回数据DTO
 * @Author gengjb
 * @Date 2023/5/23 15:42
 * @Version 1.0
 **/
@Data
@ApiModel("折线图返回数据DTO")
public class MwVisualizedFoldLineDto {

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("折线图值")
    private List values;
}
