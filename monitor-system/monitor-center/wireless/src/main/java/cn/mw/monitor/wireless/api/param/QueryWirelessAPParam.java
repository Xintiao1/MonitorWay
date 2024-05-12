package cn.mw.monitor.wireless.api.param;

import cn.mw.monitor.service.server.api.dto.QueryApplicationTableParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author syt
 * @Date 2021/6/21 10:51
 * @Version 1.0
 */
@Data
public class QueryWirelessAPParam extends QueryApplicationTableParam {
    @ApiModelProperty("排序的属性名称")
    private String sortField;
    @ApiModelProperty("0为升序；1为倒序")
    private Integer sortMode;

    @ApiModelProperty("查询字段")
    private String queryName;
    @ApiModelProperty("查询值")
    private String queryValue;
}
