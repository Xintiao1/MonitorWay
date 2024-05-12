package cn.mw.monitor.service.server.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author syt
 * @Date 2021/6/3 13:01
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ColTable {
    private String label;
    private String prop;
    private boolean sortable;
    private boolean visible;
}
