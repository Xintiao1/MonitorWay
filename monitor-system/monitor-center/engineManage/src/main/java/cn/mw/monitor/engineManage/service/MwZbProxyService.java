package cn.mw.monitor.engineManage.service;

import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.engineManage.dto.ProxyDTO;

import java.util.List;

public interface MwZbProxyService {
    /**
     * 查询引擎
     * @param dto
     * @return
     */
    public List<ProxyDTO> getProxyList(ProxyDTO dto);

    /**
     * 添加引擎
     * @param dto
     * @return
     */
    public Reply addProxy(ProxyDTO dto);

    /**
     * 删除引擎
     * @return
     */
    public Reply delProxy(List<String> proxyIds);

    /**
     * 修改
     * @return
     */
    public Reply editProxy(ProxyDTO dto);
}
