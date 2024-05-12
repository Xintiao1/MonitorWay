package cn.mw.monitor.service.server.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author syt
 * @Date 2021/4/12 20:38
 * @Version 1.0
 */
@Data
public class RankServerDTO extends ServerDTO{
    @ApiModelProperty("是否按照字母进行排序")
    private boolean sortByNameFlag;
}
