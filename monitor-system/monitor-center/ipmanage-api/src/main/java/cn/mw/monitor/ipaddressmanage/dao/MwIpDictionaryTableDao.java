package cn.mw.monitor.ipaddressmanage.dao;


import cn.mw.monitor.ipaddressmanage.dto.LabelDTO;
import cn.mw.monitor.ipaddressmanage.dto.labelOb;
import cn.mw.monitor.ipaddressmanage.param.IpDictionaryTableParam;

import java.util.List;

/**
 * @author bkc
 */
public interface MwIpDictionaryTableDao {

    //查询指定字典
    List<IpDictionaryTableParam> selectListByType(IpDictionaryTableParam qParam);


    //标签下拉查询
    List<LabelDTO> selectLabelList();

    //标签下拉查询
    List<labelOb> selectLabeipState();
}
