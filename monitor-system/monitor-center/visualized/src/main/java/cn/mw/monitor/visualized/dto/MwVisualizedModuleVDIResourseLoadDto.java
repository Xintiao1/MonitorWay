package cn.mw.monitor.visualized.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName
 * @Description VDI资源负载DTO
 * @Author gengjb
 * @Date 2023/4/25 14:34
 * @Version 1.0
 **/
@Data
@ApiModel("VDI资源负载DTO")
public class MwVisualizedModuleVDIResourseLoadDto {

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("Cpu容量")
    private String cpuCapacity;

    @ApiModelProperty("内存容量")
    private String memoryCapacity;

    @ApiModelProperty("存储容量")
    private String storageCapacity;
}
