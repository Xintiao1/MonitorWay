package cn.mw.monitor.screen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xhy
 * @date 2020/9/21 16:07
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SecurityDto {
    private String name;
    private Long value;
}
