package cn.mw.module.security.dto;

import cn.mw.monitor.bean.BaseParam;
import cn.mw.monitor.weixinapi.MwRuleSelectParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author qzg
 * @date 2021/12/22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EsSysLogTagDTO{
    @ApiModelProperty("id")
    private Integer id;
    @ApiModelProperty("id")
    private List<Integer> ids;
    @ApiModelProperty("标签名称")
    private String tagName;
    @ApiModelProperty("标签颜色")
    private String tagColor;
}
