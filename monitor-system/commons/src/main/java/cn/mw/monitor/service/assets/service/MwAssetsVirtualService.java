package cn.mw.monitor.service.assets.service;

import cn.mw.monitor.common.util.QueryHostParam;
import cn.mwpaas.common.model.Reply;

import java.util.List;

/**
 * @author syt
 * @Date 2020/6/28 11:52
 * @Version 1.0
 */
public interface MwAssetsVirtualService {
//    Reply getHostTree();
    Reply getAllTree(String type, String roleId);
    Reply getAllTree(String type);
    Reply getAllInventedAssets(String type,String uesrId,Integer userId);
    Reply getVMsTable(QueryHostParam qParam);
    Reply getVMsInfoList(QueryHostParam qParam);
}

