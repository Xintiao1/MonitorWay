package cn.mw.monitor.logManage.service;

import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.logManage.param.DictionaryAddParam;
import cn.mw.monitor.logManage.param.DictionarySearchParam;

import java.util.List;

public interface MwDictionaryService {
    ResponseBase addOrUpdateForWard(DictionaryAddParam param);

    ResponseBase deleteByIds(List<Integer> ids);

    ResponseBase pageList(DictionarySearchParam searchParam);
}
