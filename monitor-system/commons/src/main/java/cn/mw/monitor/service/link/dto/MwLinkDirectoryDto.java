package cn.mw.monitor.service.link.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author gengjb
 * @description 线路目录分类
 * @date 2024/1/12 14:23
 */
@Data
public class MwLinkDirectoryDto {

    @ApiModelProperty("目录名称")
    private String directoryName;

    @ApiModelProperty("线路ID集合")
    private List<String> linkIds;
}
