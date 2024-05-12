package cn.mw.monitor.engineManage.service;

import cn.mw.monitor.engineManage.api.param.engineManage.AddOrUpdateEngineManageParam;
import cn.mw.monitor.engineManage.api.param.engineManage.DeleteEngineManageParam;
import cn.mw.monitor.engineManage.api.param.engineManage.QueryEngineManageParam;
import cn.mwpaas.common.model.Reply;

import java.util.List;

/**
 * @author baochengbin
 * @date 2020/3/20
 */
public interface MwEngineManageService {
    Reply selectById(String id);

    Reply selectList(QueryEngineManageParam qsDTO);

    Reply update(AddOrUpdateEngineManageParam ausDTO);

    Reply delete(DeleteEngineManageParam dParam) throws Throwable;

    Reply insert(AddOrUpdateEngineManageParam ausDTO) throws Exception;

    Reply selectDropdownList(int monitorServerId,boolean selectFlag ,boolean addLocalhost);

    Reply selectDropdownBatchList(List<Integer> monitorServerIds);

    Reply fuzzSearchAllFiledData(String value);
}
