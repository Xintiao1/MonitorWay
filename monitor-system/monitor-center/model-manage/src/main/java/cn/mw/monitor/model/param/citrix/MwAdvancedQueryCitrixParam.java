package cn.mw.monitor.model.param.citrix;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date 2022/10/13
 */
@Data
public class MwAdvancedQueryCitrixParam extends BaseParam {
    @ApiModelProperty("ip开始范围")
    private String ipStart;
    @ApiModelProperty("ip结束范围")
    private String ipEnd;
    @ApiModelProperty("ip地址段")
    private String ipRange;
    @ApiModelProperty("关联的实例Id")
    private Integer relationInstanceId;
    private String queryType;//(IP ,PORT)
    private String queryValue;//查询值，输入Ip或端口的值
}
