package cn.mw.xiangtai.plugin.service.impl.detailimpl;

import cn.mw.xiangtai.plugin.domain.dto.AttackFrequencyDTO;
import cn.mw.xiangtai.plugin.domain.param.XiangtaiVisualizedParam;
import cn.mw.xiangtai.plugin.service.SyslogAlertService;
import cn.mw.xiangtai.plugin.service.XiangtaiVisualizedModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author gengjb
 * @description 攻击频率
 * @date 2023/10/19 15:02
 */
@Service
@Slf4j
public class XiangTAiattackFrequencyModule implements XiangtaiVisualizedModule {

    @Autowired
    private SyslogAlertService syslogAlertService;

    @Override
    public int[] getType() {
        return new int[]{119};
    }

    @Override
    public Object getData(XiangtaiVisualizedParam visualizedParam) {
        try {
            List<AttackFrequencyDTO> attackFrequency = syslogAlertService.getAttackFrequency();
            return attackFrequency;
        } catch (Throwable e) {
            log.error("XiangTAiattackFrequencyModule{} getData() ERROR::", e);
            return null;
        }
    }
}
