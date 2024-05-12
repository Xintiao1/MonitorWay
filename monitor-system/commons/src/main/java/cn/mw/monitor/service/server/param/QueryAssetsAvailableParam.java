package cn.mw.monitor.service.server.param;

import cn.mw.monitor.service.server.api.dto.AssetsBaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author syt
 * @Date 2021/5/25 11:26
 * @Version 1.0
 */
@Data
public class QueryAssetsAvailableParam extends AssetsBaseDTO {
    private String inBandIp;

    private int dateType;//时间范围内的数据（最近24小时/30天/自定义时间）

    private String itemId;

    private Integer value_type;

    @ApiModelProperty("当时间类型为 3:自定义时,开始时间")
    private Date dateStart;
    @ApiModelProperty("当时间类型为 3:自定义时,结束时间")
    private Date dateEnd;

    private String startDateStr;
    private String endDateStr;
}
