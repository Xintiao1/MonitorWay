package cn.mw.module.solarwind.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author xhy
 * @date 2020/7/1 21:53
 */
@Data
public class SolarHistoryDto {
    private Date lastUpdateTime;
    private String lastUpdateValue;
    private String titleName;
    private String unit;
    private List<MwHistoryDTO> dataList;
    private String caption;
}
