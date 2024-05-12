package cn.mw.monitor.ipaddressmanage.service.impl;

import cn.mw.monitor.ipaddressmanage.param.AddUpdateIpAddressManageListParam;
import cn.mw.monitor.service.user.model.MWUser;
import cn.mwpaas.common.model.Reply;

import java.util.List;

public interface ScanStrategy {
    Reply ipScan(List<AddUpdateIpAddressManageListParam> uParam
            , Integer linkId, MWUser userInfo , IcmpScanCallback icpmScanCallback) throws Exception;
}
