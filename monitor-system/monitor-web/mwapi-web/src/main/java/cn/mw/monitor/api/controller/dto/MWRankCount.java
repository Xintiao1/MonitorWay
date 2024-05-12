package cn.mw.monitor.api.controller.dto;

import lombok.Data;

/**
 * @author xhy
 * @date 2020/4/16 16:44
 */
@Data
public class MWRankCount {
    private Integer CpuCount;
    private Integer MemoryCount;
    private Integer DiskCount;
    private Integer IcmpCount;

}
