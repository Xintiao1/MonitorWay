package cn.mw.monitor.visualized.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author gengjb
 * @description 进程信息
 * @date 2023/9/14 16:04
 */
@Data
@ApiModel("进程信息")
public class MwVisualizedModuleProcessDto {

    @ApiModelProperty("进程名称")
    private String processName;

    @ApiModelProperty("进程状态")
    private String processStatus;

    @ApiModelProperty("进程Cpu")
    private String processCpu;

    @ApiModelProperty("进程内存")
    private String processMem;
}
