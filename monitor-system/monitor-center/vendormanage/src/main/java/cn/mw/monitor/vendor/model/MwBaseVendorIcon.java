package cn.mw.monitor.vendor.model;

import lombok.Data;

/**
 * @author syt
 * @Date 2021/1/20 10:42
 * @Version 1.0
 */
@Data
public class MwBaseVendorIcon {
    private Integer id;
    private String vendor;
    /**
     * 品牌小图标
     */
    private String vendorSmallIcon;
    /**
     * 品牌大图标
     */
    private String vendorLargeIcon;
}
