package cn.mw.monitor.model.param;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date 2023/5/25
 */
@Data
@ApiModel("web监测查询参数集合")
public class QueryModelWebMonitorParam extends BaseParam {

    @ApiModelProperty("主键ID")
    private Integer id;

    @ApiModelProperty("网站名称")
    private String webName;

    @ApiModelProperty("网站url")
    private String webUrl;

    private String perm;
    private Integer userId;
    private List<Integer> groupIds;
    private List<Integer> orgIds;

    private Boolean isAdmin;

    private String fuzzyQuery;

    private String modelIndex;
    private String modelId;
    private Integer instanceId;
}
