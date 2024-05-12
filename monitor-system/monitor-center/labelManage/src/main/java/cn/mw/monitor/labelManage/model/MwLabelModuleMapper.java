package cn.mw.monitor.labelManage.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author xhy
 * @date 2020/11/13 11:56
 */

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class MwLabelModuleMapper {

    private Integer id;
    private Integer labelId;
    @ApiModelProperty("模块类型id")
    private Integer moduleId;
    private Date updateTime;
    private Boolean deleteFlag;
}
