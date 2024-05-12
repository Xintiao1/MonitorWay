package cn.mw.monitor.service.topo.api;

import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.service.topo.model.MwAssetsLogoDTO;
import cn.mw.monitor.service.topo.param.InsertAssetsLogoParam;
import cn.mw.monitor.service.topo.param.QueryAssetsLogoParam;

import java.util.Map;

public interface MwAssetsLogoService {
    static final int EMPTY_TYPE = 0;
    static final String MODULE = "assets-logo";
    static final String NORMAL_LOGO = "default-normal.png";
    static final String ALERT_LOGO = "default-alert.png";
    static final String SEVERITY_LOGO = "default-severity.png";
    static final String URGENCY_LOGO = "default-urgency.png";


    Reply selectList(QueryAssetsLogoParam queryAssetsLogoParam) throws Exception;
    Reply selectById(Integer id);
    Reply update(MwAssetsLogoDTO mwAssetsLogoDTO);
    Reply delete(Integer id);
    Reply insert(InsertAssetsLogoParam insertAssetsLogoParam);
    void setLogoDir(String logoDir);
    Map<Integer, String> getAssetNormalLogo();
    Map<Integer, MwAssetsLogoDTO> getAssetLogo();
}
