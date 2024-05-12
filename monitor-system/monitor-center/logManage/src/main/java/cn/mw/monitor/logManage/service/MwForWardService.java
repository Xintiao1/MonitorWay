package cn.mw.monitor.logManage.service;

import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.logManage.param.ForWardAddParam;
import cn.mw.monitor.logManage.param.MwForwardSearchParam;

import java.util.List;

public interface MwForWardService {
    ResponseBase addForWard(ForWardAddParam param);

    ResponseBase updateForWard(ForWardAddParam param);

    ResponseBase deleteByIds(List<Integer> ids);

    ResponseBase pageList(MwForwardSearchParam searchParam);

    ResponseBase getEnumValue();

}
