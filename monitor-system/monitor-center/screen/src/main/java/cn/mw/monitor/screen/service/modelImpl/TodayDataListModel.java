package cn.mw.monitor.screen.service.modelImpl;

import cn.mw.monitor.screen.service.WebSocket;
import cn.mw.monitor.screen.dto.ModelContentDto;
import cn.mw.monitor.screen.dto.TodayDataListDto;
import cn.mw.monitor.screen.model.FilterAssetsParam;
import cn.mw.monitor.state.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author xhy
 * @date 2021/1/11 14:24
 * 27 当日资产日志量趋势
 */
@Component
public class TodayDataListModel extends BaseModel {
    private static final Logger logger = LoggerFactory.getLogger("cn/mw/monitor/screen/TodayDataListModel");

    @Override
    public void process(ModelContentDto model) {
//          ThreadPoolExecutor executorService = new ThreadPoolExecutor(9, 20, 3, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy());
//         executorService.allowCoreThreadTimeOut(true);
        String modelDataId = model.getModelDataId();
        Integer userId = model.getUserId();

        // int timeLag = dao.getBulkDataTimeCount(modelDataId, userId) == 0 ? 600 : dao.getBulkDataTime(modelDataId, userId);
        try {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    threadHashMap.put(userId + modelDataId, Thread.currentThread());

                    while (null != WebSocket.sessionPool.get(userId +modelDataId)) {
                        logger.info("{getTodayDataList}", modelDataId);
                        try {
                            FilterAssetsParam filterAssetsParam = FilterAssetsParam.builder().modelId(27).userId(userId).type(DataType.INDEX.getName()).build();
                            TodayDataListDto dto = mwModelManage.getTodayDataList(filterAssetsParam);
                            logger.info("TodayDataListModel1 "+dto);
                            webSocket.sendObjMessage(userId,modelDataId, dto);
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
