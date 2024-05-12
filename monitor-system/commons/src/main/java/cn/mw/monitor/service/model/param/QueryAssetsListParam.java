package cn.mw.monitor.service.model.param;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author qzg
 * @date 2022/4/24
 */
@Data
@ApiModel
public class QueryAssetsListParam {
    private String assetsName;
    private String assetsId;
}
