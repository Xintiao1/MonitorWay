package cn.mw.monitor.model.dto.rancher;

import lombok.Data;

/**
 * @author qzg
 * @date 2023/4/15
 */
@Data
public class RancherCapacity {
    //cpu数量
    private String cpu;
    //memory容量 单位K
    private String memory;
    //pods数量
    private String pods;
}
