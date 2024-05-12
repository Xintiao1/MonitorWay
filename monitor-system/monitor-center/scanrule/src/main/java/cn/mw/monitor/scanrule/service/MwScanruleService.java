package cn.mw.monitor.scanrule.service;

import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.scanrule.api.param.scanrule.AddScanruleParam;
import cn.mw.monitor.scanrule.api.param.scanrule.QueryScanruleParam;
import cn.mw.monitor.scanrule.api.param.scanrule.UpdateScanruleParam;

import java.util.List;


/**
 * Created by baochengbin on 2020/3/17.
 */
public interface MwScanruleService {

    Reply selectById(Integer id);

    Reply selectList(QueryScanruleParam qsDTO);

    Reply update(UpdateScanruleParam ausDTO, boolean updMapping) throws Exception;

    Reply delete(List<Integer> ids) throws Exception;

    Reply insert(AddScanruleParam ausDTO) throws Exception;

    Reply fuzzSearchAllFiledData(String value);

    Reply selectGroupServerMap(Integer assetsSubTypeId);

    Reply selectGroupServerMapList();
}
