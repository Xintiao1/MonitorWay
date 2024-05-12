package cn.mw.monitor.virtualization.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author syt
 * @Date 2020/7/6 17:14
 * @Version 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BasicDto {
    private String name;
    private String value;
}
