package cn.mw.monitor.dropDown.service;

import cn.mwpaas.common.model.Reply;

public interface MwDropDownService {

    /**
     * 根据下拉框Code查询下拉框信息
     */
    Reply selectDropdownByCode(String dropCode);

    Reply pageSelectNumUrl(String type);

    Reply pageSelectCharUrl(String type);

    Reply selectDropdown(String fieldName, String tableName);
}
