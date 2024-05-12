package cn.mw.monitor.virtualization.service;

import cn.mw.monitor.bean.TimeTaskRresult;
import cn.mw.monitor.virtualization.dto.VirtualUser;
import cn.mw.monitor.virtualization.dto.VirtualUserListPerm;
import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.common.util.QueryHostParam;

import javax.servlet.http.HttpServletResponse;

/**
 * @author syt
 * @Date 2020/6/28 11:52
 * @Version 1.0
 */
public interface MwVirtualService {
//    Reply getHostTree();
//    Reply getHostTreeTest();
    Reply getHostTable(QueryHostParam qParam);
    Reply getVMsTable(QueryHostParam qParam);
    Reply getStoreTable(QueryHostParam qParam);
    Reply getTableTitle(QueryHostParam qParam);

    Reply exportVMsTableData(QueryHostParam qParam, HttpServletResponse response);

    Reply exportHostTableData(QueryHostParam qParam, HttpServletResponse response);

    Reply exportStoreTableData(QueryHostParam qParam, HttpServletResponse response);

//    Reply getStoreTree();

    Reply getBasic(QueryHostParam qParam);

    Reply getAssetsIdByIp(QueryHostParam qParam);

    Reply setVirtualUser(VirtualUserListPerm qParam);

    Reply getVirtualUser(VirtualUser qParam);

    TimeTaskRresult saveVirtualTree();

    TimeTaskRresult saveAllVirtualListByAlert();

    Reply getAllTree(String type);
}

