package cn.mw.monitor.webMonitor.api.param.webMonitor;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author baochengbin
 * @date 2020/4/25
 */
@Data
@ApiModel("web监测查询参数集合")
public class QueryWebMonitorParam extends BaseParam {

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
}
