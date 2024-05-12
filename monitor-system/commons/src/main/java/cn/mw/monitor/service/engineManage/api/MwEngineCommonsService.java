package cn.mw.monitor.service.engineManage.api;

import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.service.engineManage.dto.MwEngineManageDTO;
import cn.mw.monitor.service.scan.model.ProxyInfo;
import cn.mw.monitor.service.scan.model.ScanResultSuccess;

import java.util.List;
import java.util.Map;

/**
 * @author syt
 * @Date 2020/11/11 9:27
 * @Version 1.0
 */
public interface MwEngineCommonsService {
    static final String LOCALHOST_KEY = "localhost";
    static final String LOCALHOST_NAME = "本机";
    /**
     * 根据isAdd判断 更新引擎关联主机数量、监控项数量
     *
     * @param isAdd    true为加  false为减
     * @param engineId
     * @param hostId
     */
    void updateMonitorNums(boolean isAdd, String engineId, String hostId);

    /**
     * 根据第三方监控服务器id删除引擎
     *
     * @param monitorServerIds
     */
    void deleteEngineByMonitorServerIds(List<Integer> monitorServerIds);

    MwEngineManageDTO selectEngineByIdNoPerm(String id);

    List<MwEngineManageDTO> selectEngineByIdsNoPerm(List<String> ids);

    //生成引擎和引擎ip的映射信息
    Map<String ,String> genProxyIpMap(List<MwTangibleassetsDTO> mwTangAssetses);
    Map<String ,String> genProxyIpMapByScanSuccess(List<ScanResultSuccess> list);
    List<ProxyInfo> genProxyIp(List<MwTangibleassetsDTO> mwTangAssetses);
    List<ProxyInfo> genProxyInfoById(String proxyId);
}
