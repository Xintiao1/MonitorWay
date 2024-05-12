package cn.mw.monitor.assets.model;

import lombok.Data;

@Data
public class MwDiskusage {
    private Integer id;
    private String hostname;
    private String ip;
    private String partition;
    private Float total;
    private Float used;
    private Float free;
}
