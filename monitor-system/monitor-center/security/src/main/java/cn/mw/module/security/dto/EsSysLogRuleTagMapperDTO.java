package cn.mw.module.security.dto;

import cn.mw.monitor.bean.BaseParam;
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
public class EsSysLogRuleTagMapperDTO extends BaseParam {
    @ApiModelProperty("id")
    private Integer id;
    @ApiModelProperty("标签id")
    private Integer tagId;
    @ApiModelProperty("标签ids")
    private List<Integer> tagIds;
    @ApiModelProperty("日志规则Id")
    private Integer ruleMapperId;
}
