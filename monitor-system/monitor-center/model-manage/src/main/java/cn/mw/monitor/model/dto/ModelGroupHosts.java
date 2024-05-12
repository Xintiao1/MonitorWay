package cn.mw.monitor.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author syt
 * @Date 2020/7/9 9:25
 * @Version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelGroupHosts {
    private String hostid;
    private String name;
}
