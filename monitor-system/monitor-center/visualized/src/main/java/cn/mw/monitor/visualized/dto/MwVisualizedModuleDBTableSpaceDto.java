package cn.mw.monitor.visualized.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @ClassName
 * @Description 数据库表空间信息DTO
 * @Author gengjb
 * @Date 2023/4/18 16:13
 * @Version 1.0
 **/
@Data
@ApiModel("数据库表空间信息DTO")
public class MwVisualizedModuleDBTableSpaceDto {

    @ApiModelProperty("主机名称")
    private String hostName;

    @ApiModelProperty("表空间使用情况")
    private List<DBTableSpaceDetailedDto> tbsSpaces;
}
