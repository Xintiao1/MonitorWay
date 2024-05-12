package cn.mw.monitor.screen.service.modelImpl;

import cn.mw.monitor.screen.dto.LinkRankDto;
import cn.mw.monitor.screen.dto.ModelContentDto;
import cn.mw.monitor.screen.service.WebSocket;
import cn.mw.monitor.state.DataType;
import cn.mwpaas.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName MWLagerScreenLinkLineChartModel
 * @Description ToDo
 * @Author gengjb
 * @Date 2022/3/17 9:40
 * @Version 1.0
 **/
@Slf4j
@Component
public class MWLagerScreenLinkLineChartModel extends BaseModel {

    @Override
    public void process(ModelContentDto model) {
        String modelDataId = model.getModelDataId();
        Integer userId = model.getUserId();
        Integer modelId = model.getModelId();
        String linkInterfaces = model.getLinkInterfaces();
        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                threadHashMap.put(userId + userId + modelDataId, Thread.currentThread());
                while (null != WebSocket.sessionPool.get(userId +modelDataId)) {
                    try {
                        String linkInterfaces1 = dao.getLinkInterfaces(null, DataType.SCREEN.getName(), modelDataId);
                        List list=new ArrayList<>();
                        if(StringUtils.isNotBlank(linkInterfaces1)){
                            list=mwModelManage.getLagerScreenLinkLineChart(linkInterfaces1);
                        }
                        log.info("MWLagerScreenLinkLineChartModel:"+list);
                        webSocket.sendObjMessage(userId,modelDataId, list);
                        Thread.sleep(120000);
                    } catch (InterruptedException e) {
                        log.error("MWLagerScreenLinkLineChartModel{}", e);
                        break;
                    }
                }
            }
        };
        executorService.execute(runnable);
    }
}
