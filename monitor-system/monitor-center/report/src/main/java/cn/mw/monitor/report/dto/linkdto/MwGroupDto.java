package cn.mw.monitor.report.dto.linkdto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author xhy
 * @date 2020/7/3 14:14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MwGroupDto {
    private Integer tag;
    private String caption;
    private Integer interfaceID;
    private String periodRadio;
    private String inColumn;
    private String outColumn;
    private Float percentFront;
    private Float percentBack;
    private String tableName;
    private String startTime;
    private String endTime;
    private String startTimeDay;
    private String endTimeDay;
    private List<String> interfaceIDs;
}
