package cn.mw.monitor.report.dto.assetsdto;

import cn.mw.monitor.bean.BaseParam;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class RunTimeQueryParam  extends BaseParam {
    private Date dateStart;
    private Date dateEnd;
    //自定义时间
    private List<String> chooseTime;
    //zabbix监控项名
    private String itemName;
    //topN
    private Integer dataSize;
    private Integer reportItemType;
    private Integer dateType;
    private Integer trendType;
    private List<String> ids;

    private String searchName;

    //是否定时任务类型
    private Boolean timingType;

    private int reportType;
}
