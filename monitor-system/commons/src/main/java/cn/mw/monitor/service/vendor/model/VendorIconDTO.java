package cn.mw.monitor.service.vendor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author syt
 * @Date 2021/1/20 12:09
 * @Version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorIconDTO {
    private int id;

    /**
     * 品牌小图标
     */
    private String vendorSmallIcon;
    /**
     * 品牌大图标
     */
    private String vendorLargeIcon;
    /**
     * 图标类型
     */
    private Integer customFlag;
}
