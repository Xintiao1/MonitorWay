package cn.mw.monitor.server.param;

import cn.mw.monitor.service.server.api.dto.AssetsBaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author syt
 * @Date 2021/1/18 10:05
 * @Version 1.0
 */
@Data
public class QueryHardwareParam extends AssetsBaseDTO {
    @ApiModelProperty("表格类型对应的应用集名称")
    private List<String> applicationNames;
    //监控项名称获取
    private String typeName;

    // 第几页
    @ApiModelProperty("第几页")
    private Integer pageNumber = 1;
    // 每页显示行数
    @ApiModelProperty("每页显示行数")
    private Integer pageSize = 20;

    private boolean limitFlag;

}
