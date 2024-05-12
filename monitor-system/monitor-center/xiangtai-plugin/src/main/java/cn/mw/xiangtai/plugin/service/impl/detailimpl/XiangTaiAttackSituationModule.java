package cn.mw.xiangtai.plugin.service.impl.detailimpl;

import cn.mw.xiangtai.plugin.domain.param.XiangtaiVisualizedParam;
import cn.mw.xiangtai.plugin.service.SyslogAlertService;
import cn.mw.xiangtai.plugin.service.XiangtaiVisualizedModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author gengjb
 * @description 攻击情况汇总
 * @date 2023/10/19 14:41
 */
@Service
@Slf4j
public class XiangTaiAttackSituationModule implements XiangtaiVisualizedModule {

    @Autowired
    private SyslogAlertService syslogAlertService;

    @Override
    public int[] getType() {
        return new int[]{114};
    }

    @Override
    public Object getData(XiangtaiVisualizedParam visualizedParam) {
        try {
            Integer toDayAttackCount = syslogAlertService.lambdaQuery().last("where create_time >= toStartOfDay(now()) and create_time <= now()").count();
            return toDayAttackCount;
        }catch (Throwable e){
            log.error("XiangTaiAttackSituationModule{} getData() ERROR::",e);
            return null;
        }
    }
}
