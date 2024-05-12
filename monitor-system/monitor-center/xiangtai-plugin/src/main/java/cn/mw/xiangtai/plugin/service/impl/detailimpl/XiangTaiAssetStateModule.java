package cn.mw.xiangtai.plugin.service.impl.detailimpl;

import cn.mw.xiangtai.plugin.domain.dto.XiangtaiAssetsInfoDto;
import cn.mw.xiangtai.plugin.domain.param.XiangtaiVisualizedParam;
import cn.mw.xiangtai.plugin.monitor.dao.XiangtaiLogVisualizedMapper;
import cn.mw.xiangtai.plugin.service.XiangtaiVisualizedModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author gengjb
 * @description 祥泰资产态势
 * @date 2023/10/19 15:00
 */
@Service
@Slf4j
public class XiangTaiAssetStateModule implements XiangtaiVisualizedModule {

    @Autowired
    private XiangtaiLogVisualizedMapper logVisualizedMapper;

    @Override
    public int[] getType() {
        return new int[]{118};
    }

    @Override
    public Object getData(XiangtaiVisualizedParam visualizedParam) {
        try {
            XiangtaiAssetsInfoDto xiangtaiAssetsInfoDto = logVisualizedMapper.selectXiangtaiAssetsInfo();
            return xiangtaiAssetsInfoDto;
        } catch (Throwable e) {
            log.error("XiangTaiAssetStateModule{} getData() ERROR::", e);
            return null;
        }
    }
}
