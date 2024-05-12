package cn.mw.monitor.assets.dto;

import lombok.Data;

import java.util.List;

/**
 * @author syt
 * @Date 2020/5/25 18:30
 * @Version 1.0
 */
@Data
public class AssetsValuemapDto {
    private String valuemapid;
    private String name;
    private List<AssetsValueMappingDto> mappings;
}
