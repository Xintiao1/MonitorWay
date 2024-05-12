package cn.mw.monitor.service.model.param;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author qzg
 * @date 2022/4/24
 */
@Data
@ApiModel
public class QueryBladeInstanceParam {
    private String instanceName;
    private String instanceId;
    //是否当前资产标识
    private boolean currentFlag;
}
