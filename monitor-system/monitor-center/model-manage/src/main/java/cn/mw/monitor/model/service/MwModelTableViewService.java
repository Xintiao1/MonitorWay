package cn.mw.monitor.model.service;

import cn.mw.monitor.api.param.user.QueryUserParam;
import cn.mw.monitor.model.param.ModelTableViewParam;
import cn.mwpaas.common.model.Reply;

import javax.servlet.http.HttpServletResponse;

/**
 * @author guiquanwnag
 * @datetime 2023/6/30
 * @Description ARP_MAC_IP数据服务类
 */
public interface MwModelTableViewService {

    Reply getTableView(ModelTableViewParam param);


    /**
     * 导出excel模板
     *
     * @param response 导出数据
     * @param param    数据
     */
    void exportResultExcel(HttpServletResponse response, ModelTableViewParam param);
}
