package cn.mw.monitor.server.service;

import cn.mwpaas.common.model.Reply;

/**
 * @author syt
 * @Date 2020/12/17 10:06
 * @Version 1.0
 */
public interface MwStorageService {
    /**
     * 获取存储卷信息
     * @param monitorServerId
     * @param hostId
     * @param typeName
     * @return
     */
    Reply getStorageVolInfo(int monitorServerId, String hostId, String typeName);

    /**
     * 获取所有相似的监控项除去分区的名称信息
     * @param monitorServerId
     * @param hostId
     * @param itemName
     * @return
     */
    Reply getItemNameLikes(int monitorServerId, String hostId, String itemName);

}
