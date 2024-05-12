package cn.mw.monitor.service.model.param;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author qzg
 * @date 2023/03/25 10:14
 */
@Data
@ApiModel
public class QueryDigitalTwinParam {
    //设备Id
    private String id;
}
