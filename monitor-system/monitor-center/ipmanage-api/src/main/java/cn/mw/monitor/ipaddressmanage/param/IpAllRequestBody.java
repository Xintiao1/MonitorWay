package cn.mw.monitor.ipaddressmanage.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author lumingming
 * @createTime 2023529 9:32
 * @description ip地址继承
 */

@Data
@ApiModel("ip数据地域表")
@Accessors(chain = true)
public class IpAllRequestBody {

    @ApiModelProperty(value="ip数据地域表id")
    private Integer id;


    @ApiModelProperty(value="命名空间")
    private String namespace;

    @ApiModelProperty(value="ip数据地域表id")
    private Integer signId;


}
