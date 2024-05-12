package cn.mw.monitor.model.service;

import cn.mw.monitor.bean.TimeTaskRresult;
import cn.mw.monitor.common.util.QueryHostParam;
import cn.mw.monitor.model.param.ModelRelationInstanceUserListParam;
import cn.mw.monitor.model.param.ModelRelationInstanceUserParam;
import cn.mw.monitor.model.param.virtual.ModelVirtualUserListParam;
import cn.mw.monitor.model.param.virtual.ModelVirtualUserParam;
import cn.mw.monitor.model.param.virtual.QueryVirtualInstanceParam;
import cn.mw.monitor.service.model.dto.ModelVirtualDeleteContext;
import cn.mw.monitor.service.scan.model.ProxyInfo;
import cn.mwpaas.common.model.Reply;

import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author qzg
 * @date 2022/9/7
 */
public interface MwModelVirtualizationService {
    Reply syncVirtualDeviceInfo(QueryVirtualInstanceParam param) throws Exception;

    Reply getVirtualDeviceTree(QueryVirtualInstanceParam param);

    Reply getVirtualDeviceBaseInfo(QueryVirtualInstanceParam param);

    Reply getVirtualDeviceInfoList(QueryVirtualInstanceParam param);

    Reply getVirtualMonitorInfoByHistory(QueryVirtualInstanceParam param);

    Reply getVirDeviceByPieSimple(QueryVirtualInstanceParam param);

    Reply getAllVirtualInfo();

    Reply exportVirtualList(QueryVirtualInstanceParam param, HttpServletResponse response);

    Reply setVirtualUser(ModelRelationInstanceUserListParam param);

    Reply getVirtualUser(ModelRelationInstanceUserParam param);

    Reply queryAssetsInfo(QueryHostParam qParam);

    ModelVirtualDeleteContext deleteVirtualIntance(Integer instanceId);

    TimeTaskRresult getVCenterInfoByTaskTime();
}
