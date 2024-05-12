package cn.mw.monitor.service.alert.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author syt
 * @date 2020/5/7
 */
@Data
public class DiscDto {
    @ApiModelProperty("磁盘数据/磁盘Io数据")
    private List<ItemData> infoData;
    @ApiModelProperty("磁盘/磁盘Io")
    private String infoName;

}
