package cn.mw.monitor.visualized.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName
 * @Description 机房信息DTO
 * @Author gengjb
 * @Date 2023/5/18 15:30
 * @Version 1.0
 **/
@Data
@ApiModel("机房信息DTO")
public class MwVisualizedResourceRoomDto {

    @ApiModelProperty("机房名称")
    private String roomName;

    @ApiModelProperty("总数")
    private Integer sumCount;

    @ApiModelProperty("上架数")
    private String groundingCount;

    @ApiModelProperty("空闲数")
    private String idleCount;
}
