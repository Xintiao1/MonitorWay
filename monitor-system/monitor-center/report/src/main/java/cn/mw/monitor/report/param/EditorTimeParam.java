package cn.mw.monitor.report.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author xhy
 * @date 2020/6/29 10:22
 */
@Data
public class EditorTimeParam {
    private Integer id;
    private List<String> period;
    @ApiModelProperty("0表示solar设置的时间,1磁盘报表设置的时间,2网络报表设置的时间，3cpu和内存报表设置的时间")
    private Integer type;
    @ApiModelProperty("yyyyMMdd")
    private String exportDate;
}
