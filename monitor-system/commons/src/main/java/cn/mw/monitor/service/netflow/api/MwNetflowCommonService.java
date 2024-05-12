package cn.mw.monitor.service.netflow.api;

import cn.mw.monitor.service.netflow.entity.NetflowResult;
import cn.mw.monitor.service.netflow.param.NetflowSearchParam;

import java.util.List;

/**
 * @author guiquanwnag
 * @datetime 2023/8/28
 * @Description 流量公共接口
 */
public interface MwNetflowCommonService {

    /**
     * 根据查询参数，获取流量数据
     *
     * @param searchParam 流量查询数据
     * @return
     */
    List<NetflowResult> getNetflowResult(NetflowSearchParam searchParam);

}
