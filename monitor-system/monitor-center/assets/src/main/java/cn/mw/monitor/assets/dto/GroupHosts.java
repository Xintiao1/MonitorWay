package cn.mw.monitor.assets.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author syt
 * @Date 2020/7/9 9:25
 * @Version 1.0
 */
@Data
@Builder
public class GroupHosts {
    private String hostid;
    private String name;
}
