package cn.mw.monitor.service.vendor.api;

import cn.mwpaas.common.model.Reply;

/**
 * @author syt
 * @Date 2021/1/20 14:07
 * @Version 1.0
 */
public interface MWVendorDropDown {
    Reply selectVendorDropdownList(String specification, boolean selectFlag);

    Reply selectVModelDropdownList(String vendor);
}
