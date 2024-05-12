package cn.mw.monitor.TPServer.service;

import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.TPServer.dto.AddOrUpdateTPServerParam;
import cn.mw.monitor.TPServer.dto.QueryTPServerParam;

import java.util.List;

/**
 * @author syt
 * @Date 2020/10/30 15:58
 * @Version 1.0
 */
public interface MwTPServerService {
    Reply selectById(Integer id);

    Reply selectList(QueryTPServerParam qsDTO);

    Reply update(AddOrUpdateTPServerParam ausDTO);

    Reply delete(List<Integer> ids) throws Throwable;

    Reply insert(AddOrUpdateTPServerParam ausDTO);

    Reply selectDropdownListByType(boolean selectFlag);

    Reply fuzzSearchAllFiledData(String value);

    Reply getZabbixTemplateSession(String hostIp);

}
