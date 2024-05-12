package cn.mw.monitor.assets.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class MwGroupHostDTO {
    private String hostid;
    private String host;
    private List<MwIpDTO> interfaces;
    private Map<String, MwPartitionInfoDTO> partitions;
}
