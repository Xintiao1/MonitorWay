package cn.mw.monitor.visualized.dto;

import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @ClassName
 * @Description 中控机柜Dto
 * @Author gengjb
 * @Date 2023/3/15 15:19
 * @Version 1.0
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MwVisuZkSoftWareServerRoomDto {

    @ApiModelProperty("机柜名称")
    private String serverRoomName;

    @ApiModelProperty("机柜状态")
    private String serverRoomStatus;

    @ApiModelProperty("机柜异常信息")
    private String abNormalMessage;

    @ApiModelProperty("所属机组")
    private String machineSet;

    @ApiModelProperty("属于机柜的资产信息")
    private List<MwTangibleassetsDTO> tangibleassetsDTOList;
}
