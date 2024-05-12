package cn.mw.monitor.screen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xhy
 * @date 2020/9/1 15:43
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageCount {
    private Integer totalCount;
    private Integer successCount;
    private Integer failedCount;
}
