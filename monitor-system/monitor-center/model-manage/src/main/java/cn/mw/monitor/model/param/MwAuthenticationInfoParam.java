package cn.mw.monitor.model.param;

import cn.mw.monitor.service.assets.param.Macros;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date 2022/11/14
 */
@ApiModel
@Data
public class MwAuthenticationInfoParam {
    @ApiModelProperty("id")
    private Integer id;
    @ApiModelProperty("模板名称")
    private String templateName;
    @ApiModelProperty("凭证名称")
    private String profileName;
    @ApiModelProperty("凭证值")
    private String valueJson;
    @ApiModelProperty(value = "宏值")
    private List<Macros> mwMacrosDTO;
}
