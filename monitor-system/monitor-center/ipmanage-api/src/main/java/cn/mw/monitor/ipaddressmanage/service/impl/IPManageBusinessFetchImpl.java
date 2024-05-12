package cn.mw.monitor.ipaddressmanage.service.impl;

import cn.mw.monitor.service.alert.callback.BusinessIds;
import cn.mw.monitor.service.alert.callback.BusinessIdsFetch;
import cn.mw.monitor.state.DataType;

import java.util.ArrayList;
import java.util.List;

public class IPManageBusinessFetchImpl implements BusinessIdsFetch {
    private List<Integer> linkIds;


    public IPManageBusinessFetchImpl(List<Integer> linkIds){
        this.linkIds = linkIds;
    }

    @Override
    public BusinessIds getBusinessIds() {
        BusinessIds businessIds = new BusinessIds();
        businessIds.setDataType(DataType.IP);
        List<String> list = new ArrayList<>();
        for(Integer id : linkIds){
            list.add(id.toString());
        }
        businessIds.setBusinessIds(list);
        return businessIds;
    }
}
