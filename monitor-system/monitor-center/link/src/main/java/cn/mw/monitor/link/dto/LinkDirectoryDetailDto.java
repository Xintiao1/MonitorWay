package cn.mw.monitor.link.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author gengjb
 * @description 线路目录与线路明细DTO
 * @date 2023/8/21 15:32
 */
@Data
@ApiModel("线路目录与线路明细DTO")
public class LinkDirectoryDetailDto {

    @ApiModelProperty("目录名称")
    private String contentsName;

    @ApiModelProperty("目录ID")
    private String treeId;

    @ApiModelProperty("线路ID")
    private String linkId;

    @ApiModelProperty("线路明细")
    private List<NetWorkLinkDto> workLinkDtos;
}
