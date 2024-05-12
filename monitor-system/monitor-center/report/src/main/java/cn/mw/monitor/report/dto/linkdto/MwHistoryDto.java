package cn.mw.monitor.report.dto.linkdto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author xhy
 * @date 2020/12/28 9:36
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MwHistoryDto {
    private Date date;
    private String value;
}
