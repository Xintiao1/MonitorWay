package cn.mw.monitor.alert.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author xhy
 * @date 2020/7/29 11:37
 */
@Data
public class MWAlertHistoryDto {
    private Date lastUpdateTime;

    private List<MwHistoryDTO> dataList;

    private String unit;

    private String title;
}
