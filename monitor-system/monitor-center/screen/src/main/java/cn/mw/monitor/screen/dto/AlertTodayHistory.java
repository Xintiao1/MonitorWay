package cn.mw.monitor.screen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * @author xhy
 * @date 2020/9/2 10:46
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertTodayHistory {
//    private Integer count;
//    private Date date;

    private List<String> date;
    private List<Long> count;
}
