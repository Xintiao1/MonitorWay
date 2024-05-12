package cn.mw.monitor.report.dto.assetsdto;

import cn.mw.monitor.bean.BaseParam;
import lombok.Data;

import java.util.List;

/**
 * @author lumingming
 * @createTime 202111/0505 15:03
 * @description
 */
@Data
public class IpReportSreach   extends BaseParam {
    private List<String> ids;
    private Integer radio;
    private Integer dateType;
    private List<String> chooseTime;
    private Integer status;
    private String property;
    private String order;
}
