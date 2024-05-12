package cn.mw.xiangtai.plugin.service.impl.detailimpl;

import cn.mw.xiangtai.plugin.domain.dto.LogPointDTO;
import cn.mw.xiangtai.plugin.domain.param.EventParam;
import cn.mw.xiangtai.plugin.domain.param.XiangtaiVisualizedParam;
import cn.mw.xiangtai.plugin.enums.XiangtaiDateTypeEnum;
import cn.mw.xiangtai.plugin.service.XiangtaiVisualizedModule;
import cn.mw.xiangtai.plugin.service.XiangtaiVisualizedService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author gengjb
 * @description 日志事件
 * @date 2023/10/17 9:53
 */
@Service
@Slf4j
public class XiangtaiLogEventModule implements XiangtaiVisualizedModule {

    @Autowired
    private XiangtaiVisualizedService visualizedService;

    @Override
    public int[] getType() {
        return new int[]{110};
    }

    @Override
    public Object getData(XiangtaiVisualizedParam visualizedParam) {
        try {
            EventParam param= new EventParam();
            param.setDateType(visualizedParam.getDateType());
            param.setInterval(XiangtaiDateTypeEnum.getIntervalByType(visualizedParam.getDateType()).getInterval());
            List<LogPointDTO> logEvents = visualizedService.logEventCount(param);
            return logEvents;
        }catch (Throwable e){
            log.error("XiangtaiLogEventModule{} getData() ERROR::",e);
            return null;
        }
    }
}
