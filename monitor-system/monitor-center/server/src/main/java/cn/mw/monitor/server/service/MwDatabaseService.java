package cn.mw.monitor.server.service;

import cn.mwpaas.common.model.Reply;

import java.util.List;

/**
 * @author syt
 * @Date 2020/12/10 10:36
 * @Version 1.0
 */
public interface MwDatabaseService {
    //获取监控项中名字有关联关系的所有监控项，并获取它们的最新数据
    Reply getRelevantInfoForTable(int monitorServerId, String hostId, String itemName);

    //获取mysql 中每秒查询、每秒问题、每秒慢查询的监控项，并获取它们的最新数据
    Reply getSelectInfoForTable(int monitorServerId, String hostId);

    //获取监控项们的最新数据
    Reply getRelevantInfoForTable(int monitorServerId, String hostId, List<String> itemNames);
}
