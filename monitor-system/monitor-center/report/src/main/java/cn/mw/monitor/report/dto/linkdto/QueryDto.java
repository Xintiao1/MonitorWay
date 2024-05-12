package cn.mw.monitor.report.dto.linkdto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author xhy
 * @date 2020/12/26 13:20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryDto {
    private String tableName;
    private String startTime;
    private String endTime;
    private List<String> objectIds;
}
