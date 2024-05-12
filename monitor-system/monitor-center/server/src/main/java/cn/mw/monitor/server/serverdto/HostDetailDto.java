package cn.mw.monitor.server.serverdto;

import lombok.Data;

/**
 * @author syt
 * @Date 2020/6/8 16:19
 * @Version 1.0
 */
@Data
public class HostDetailDto {
    private String status;
    private String duration;

    private Integer cpuCores;

    private String totalMemory;
}
