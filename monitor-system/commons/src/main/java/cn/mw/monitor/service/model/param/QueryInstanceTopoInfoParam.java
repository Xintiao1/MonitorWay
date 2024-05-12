package cn.mw.monitor.service.model.param;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author xhy
 * @date 2021/2/25 16:07
 */
@Data
@ApiModel
public class QueryInstanceTopoInfoParam extends BaseParam {
    private String ownModelId;
    private String ownInstanceId;
    private String raletionModelId;
    private String deep;
}
