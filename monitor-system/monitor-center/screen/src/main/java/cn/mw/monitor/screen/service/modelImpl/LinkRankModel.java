package cn.mw.monitor.screen.service.modelImpl;

import cn.mw.monitor.screen.dto.LinkRankDto;
import cn.mw.monitor.screen.dto.ModelContentDto;
import cn.mw.monitor.screen.service.WebSocket;
import cn.mwpaas.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class LinkRankModel extends BaseModel{
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
                        List<LinkRankDto> list=new ArrayList<>();
                        if(StringUtils.isNotBlank(linkInterfaces)){
                            list=mwModelManage.getLinkRank(linkInterfaces,userId);
                        }
                        log.info("ScreenLinkRankModel:"+list);
                        webSocket.sendObjMessage(userId,modelDataId, list);
                        Thread.sleep(1000 * model.getTimeLag());
                    } catch (InterruptedException e) {
                        log.error("线程执行ScreenLinkRankModel{}", e);
                        break;
                    }
                }
            }
        };
        executorService.execute(runnable);
    }
}
