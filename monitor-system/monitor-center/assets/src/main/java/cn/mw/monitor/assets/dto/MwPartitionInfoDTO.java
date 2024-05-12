package cn.mw.monitor.assets.dto;

import lombok.Data;

@Data
public class MwPartitionInfoDTO {
    private String partition;
    private String unit;
    private float free;
    private float total;
    private float used;
}
