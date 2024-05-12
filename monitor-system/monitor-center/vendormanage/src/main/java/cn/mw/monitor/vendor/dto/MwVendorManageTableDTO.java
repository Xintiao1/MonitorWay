package cn.mw.monitor.vendor.dto;

import cn.mw.monitor.service.vendor.model.VendorIconDTO;
import cn.mw.monitor.vendor.model.MwVendorManageTable;
import lombok.Data;

/**
 * @author syt
 * @Date 2021/1/20 10:09
 * @Version 1.0
 */
@Data
public class MwVendorManageTableDTO extends MwVendorManageTable {
    //厂商对应的大图标和小图标
    private VendorIconDTO vendorIconDTO;
}
