package cn.mw.monitor.prometheus.dto;

import lombok.Data;

@Data
public class QueryConfigDto {
    private int id;
    private String queryName;
    private String querySql;
    private boolean state;
    private String unit;
}
