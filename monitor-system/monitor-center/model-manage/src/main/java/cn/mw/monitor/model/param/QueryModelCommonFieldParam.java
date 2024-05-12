package cn.mw.monitor.model.param;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date 2023/05/15 9:25
 */
@Data
@ApiModel
public class QueryModelCommonFieldParam {
    private boolean queryAllParentField;
}
