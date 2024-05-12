package cn.mw.monitor.screen.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * @author xhy
 * @date 2020/5/27 16:45
 */
@Data
@ApiModel
@Component
public class ModelContentDto {
    @ApiModelProperty("组件id")
    private Integer modelId;
    @ApiModelProperty("组件数据id")
    private String modelDataId;
    @ApiModelProperty("组件内容")
    private String modelContent;
    @ApiModelProperty("资产id")
    private Integer assetsTypeId;
    @ApiModelProperty("监控项名称")
    private String itemName;
    @ApiModelProperty("组件类型")
    private String modelType;
    @ApiModelProperty("类名")
    private String className;
    @ApiModelProperty("当前用户登录id")
    private Integer userId;
    private Integer timeLag;
    private String linkInterfaces;
}
