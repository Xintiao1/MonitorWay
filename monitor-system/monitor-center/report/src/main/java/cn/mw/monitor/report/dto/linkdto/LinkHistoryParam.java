package cn.mw.monitor.report.dto.linkdto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * @author xhy
 * @date 2020/12/28 9:48
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkHistoryParam {
    private String tableName;
    private String interfaceID;
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
