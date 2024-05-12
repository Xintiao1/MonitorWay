package cn.mw.monitor.screen.service.modelImpl;

import cn.mw.monitor.screen.service.WebSocket;
import cn.mw.monitor.screen.dto.ModelContentDto;
import cn.mw.monitor.screen.model.FilterAssetsParam;
import cn.mw.monitor.state.DataType;;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author xhy
 * @date 2021/1/11 14:47
 * 29 资产日志量总统计
 */
@Component
public class LogCountModel extends BaseModel {
    private static final Logger logger = LoggerFactory.getLogger("cn/mw/monitor/screen/LogCountModel");

    @Override
    public void process(ModelContentDto model) {
//        ThreadPoolExecutor executorService = new ThreadPoolExecutor(9, 20, 3, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy());
//        executorService.allowCoreThreadTimeOut(true);
        String modelDataId = model.getModelDataId();
        Integer userId = model.getUserId();

        try {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    threadHashMap.put(userId+modelDataId,Thread.currentThread());
                    while (null != WebSocket.sessionPool.get(userId +modelDataId)) {
                        logger.info("{getHostCountByLog}", modelDataId);
                        try {
                        FilterAssetsParam filterAssetsParam = FilterAssetsParam.builder().modelId(28).userId(userId).type(DataType.INDEX.getName()).build();
                        Map map = (Map) mwModelManage.getLogCount(filterAssetsParam);
                        webSocket.sendObjMessage(userId,modelDataId, map);
                            Thread.sleep(1000 * model.getTimeLag());
                        } catch (InterruptedException e) {
                            logger.error("线程执行InterruptedException{}", e);
                            break;
                        }
                    }
                }
            };
            executorService.execute(runnable);
        } catch (Exception e) {
            logger.error("SCREEN_LOG[]screen[]大屏[]查询大屏的组件数据[]{}", e);
        }
    }
}
