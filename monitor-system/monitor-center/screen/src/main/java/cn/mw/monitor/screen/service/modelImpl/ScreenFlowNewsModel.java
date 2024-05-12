package cn.mw.monitor.screen.service.modelImpl;

import cn.mw.monitor.screen.dto.ItemRank;
import cn.mw.monitor.screen.dto.ModelContentDto;
import cn.mw.monitor.screen.model.FilterAssetsParam;
import cn.mw.monitor.screen.service.WebSocket;
import cn.mw.monitor.state.DataType;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName ScreenFlowNewsModel
 * @Description 监控大屏流量信息ws接口
 * @Author gengjb
 * @Date 2022/8/1 10:17
 * @Version 1.0
 **/
@Component
public class ScreenFlowNewsModel extends BaseModel{

    @Override
    public void process(ModelContentDto model) {
        String modelDataId = model.getModelDataId();
        Integer userId = model.getUserId();
        List<String> itemNames = new ArrayList<>();
        switch (model.getModelId()) {
            case 37:
                itemNames.add("INTERFACE_IN_UTILIZATION");
                itemNames.add("INTERFACE_OUT_UTILIZATION");
                processHostRank(model, modelDataId, userId,itemNames);
                break;
            case 38:
                itemNames.add("INTERFACE_IN_TRAFFIC");
                itemNames.add("INTERFACE_OUT_TRAFFIC");
                processHostRank(model, modelDataId, userId,itemNames);
                break;
            default:
                break;
        }
    }


    private void processHostRank(ModelContentDto model, String modelDataId, Integer userId, List<String> names) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                threadHashMap.put(userId + modelDataId, Thread.currentThread());
                while (null != WebSocket.sessionPool.get(userId + modelDataId)) {
                    try {
                        String redislist = redisTemplate.opsForValue().get(genRedisKey("getAlertEvent_getHostRank", modelDataId, userId));
                        ItemRank itemRank = new ItemRank();
                        if (null != redislist && StringUtils.isNotEmpty(redislist)) {
                            itemRank = JSONObject.parseObject(redislist, ItemRank.class);
                        } else {
                            FilterAssetsParam filterAssetsParam = FilterAssetsParam.builder().modelId(model.getModelId()).modelDataId(modelDataId).userId(userId).type(DataType.SCREEN.getName()).build();
                            itemRank = mwModelManage.getScreenFlowNews(names, filterAssetsParam,null,10);
                        }
                        webSocket.sendObjMessage(userId, modelDataId, itemRank);
                        Thread.sleep(1000 * model.getTimeLag());
                    } catch (Exception e) {
                        break;
                    }
                }
            }
        };
        executorService.execute(runnable);
    }
}
