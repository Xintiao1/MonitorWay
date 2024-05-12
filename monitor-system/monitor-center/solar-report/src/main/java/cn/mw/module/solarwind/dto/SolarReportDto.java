package cn.mw.module.solarwind.dto;

import cn.mw.monitor.bean.BaseParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;


/**
 * @author xhy
 * @date 2020/6/24 9:48
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SolarReportDto extends BaseParam {
    private String tableName;
    private Integer interfaceID;
    private String[] tableNames;
    private String startTime;
    private String endTime;
    private String carrierName;
    private String startTimeDay;
    private String endTimeDay;
    private String periodRadio;
    private String inColumn;
    private String outColumn;
    private Date dateTime;
    private List<Integer> interfaceIDs;
}
