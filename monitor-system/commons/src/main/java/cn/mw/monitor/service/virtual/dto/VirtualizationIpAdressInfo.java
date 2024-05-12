package cn.mw.monitor.service.virtual.dto;

import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * 虚拟化设备监控数据
 * @date 2022/9/15
 */
@Data
public class VirtualizationIpAdressInfo {
    private String ip;
    private boolean isMatch;

}
