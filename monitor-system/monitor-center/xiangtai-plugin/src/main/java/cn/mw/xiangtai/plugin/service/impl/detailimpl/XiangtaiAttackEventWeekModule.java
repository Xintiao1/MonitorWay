package cn.mw.xiangtai.plugin.service.impl.detailimpl;

import cn.mw.xiangtai.plugin.domain.dto.LogPointDTO;
import cn.mw.xiangtai.plugin.domain.param.XiangtaiVisualizedParam;
import cn.mw.xiangtai.plugin.service.XiangtaiVisualizedModule;
import cn.mw.xiangtai.plugin.service.XiangtaiVisualizedService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author gengjb
 * @description 近一周攻击数据
 * @date 2023/10/17 10:01
 */
@Service
@Slf4j
public class XiangtaiAttackEventWeekModule implements XiangtaiVisualizedModule {

    @Autowired
    private XiangtaiVisualizedService visualizedService;

    @Override
    public int[] getType() {
        return new int[]{113};
    }

    @Override
    public Object getData(XiangtaiVisualizedParam visualizedParam) {
        try {
            List<LogPointDTO> pointDTOS = visualizedService.attackEventCountByWeek();
            return pointDTOS;
        }catch (Throwable e){
            log.error("XiangtaiAttackEventWeekModule{} getData() ERROR::",e);
            return null;
        }
    }
}
