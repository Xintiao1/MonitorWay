package cn.mw.monitor.report.dto.linkdto;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author xhy
 * @date 2020/12/28 9:34
 */
@Data
public class LinkHistoryDto {
    private Date lastUpdateTime;
    private String lastUpdateValue;
    private String titleName;
    private String unit;
    private List<MwHistoryDto> dataList;
    private String caption;
}
