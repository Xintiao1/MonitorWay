package cn.mw.monitor.model.param;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date 2022/9/15
 */
@Data
public class QueryModelViewInstanceParam extends BaseParam {
    @ApiModelProperty("自动扫描成功结果Id")
    private Integer scanSuccessId;

    private String inBandIp;
}
