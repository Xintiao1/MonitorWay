package cn.mw.monitor.api.controller;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.manager.dto.MwAssetsIdsDTO;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author syt
 * @Date 2020/11/17 10:04
 * @Version 1.0
 */
@RequestMapping("/mwapi")
@Controller
@Slf4j
public class MWRedisConfigController extends BaseApiService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private MWTPServerAPI mwtpServerAPI;
    @Autowired
    private MwAssetsManager mwAssetsManager;

    private static final Map<Integer, String> timeTypeValue = new HashMap<>();

    static {
        timeTypeValue.put(1, "15mins:");
        timeTypeValue.put(2, "60mins:");
    }

    /**
     * 初始化更新redis有关一周一月的数据
     */
    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/redisItems/update")
    @ResponseBody
    public ResponseBase updateRedisItemsData(HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = initDeleteItemsHistory();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("updateRedisItemsData", e);
            reply = Reply.fail("初始化更新redis有关一周一月的数据失败");
            return setResultFail(reply.getMsg(), reply.getData());
        }
        return setResultSuccess(reply);
    }

    private Reply initUpdateItemsHistory() {
        List<MwAssetsIdsDTO> assetsIds = mwAssetsManager.getAssetsIds(false);
        if (assetsIds != null && !assetsIds.isEmpty()) {
            assetsIds.forEach(assetsId -> {
                //筛选监控项信息类型为0或者3,关联映射值的id为0的主机为assetsId.getHostId()下的监控项
                int monitorServerId = assetsId.getMonitorServerId();
                MWZabbixAPIResult items = mwtpServerAPI.getItemsByHostIdFilter(monitorServerId, assetsId.getHostId(), Arrays.asList("0", "3"), "0",null,null);
                if (!items.isFail()) {
                    JsonNode map = (JsonNode) items.getData();
                    if (map.size() > 0) {
                        for (JsonNode item : map) {
                            //获取原先key值
                            for (int i = 1; i < 3; i++) {
                                String oldKey = timeTypeValue.get(i) + item.get("itemid").asText() + item.get("name").asText();
                                String newKey = assetsId.getId() + ":" + timeTypeValue.get(i) + item.get("itemid").asText() + item.get("name").asText();
                                if (redisTemplate.hasKey(oldKey)) {
                                    //从排序集中获取开始和结束之间的元组
                                    Set<ZSetOperations.TypedTuple<String>> typedTuples = redisTemplate.opsForZSet().rangeWithScores(oldKey, 0, -1);
                                    //存到新的key中
                                    redisTemplate.opsForZSet().add(newKey, typedTuples);
                                    redisTemplate.delete(oldKey);
                                }
                            }
                        }
                    }
                }
            });
        }
        Set<String> keys = redisTemplate.keys(timeTypeValue.get(1) + "*");
        keys.addAll(redisTemplate.keys(timeTypeValue.get(2) + "*"));
        keys.forEach(key -> {
            redisTemplate.delete(key);
        });
        //删除redis中已经被删除的资产的存储
        List<MwAssetsIdsDTO> assetsIdsByDeleted = mwAssetsManager.getAssetsIds(true);
        assetsIdsByDeleted.forEach(assetsIdDelete->{
            redisTemplate.delete(redisTemplate.keys(assetsIdDelete.getId() + "*"));
        });
        return Reply.ok();
    }


    private Reply initDeleteItemsHistory() {
        List<MwAssetsIdsDTO> assetsIds = mwAssetsManager.getAssetsIds(false);
        assetsIds.addAll(mwAssetsManager.getAssetsIds(true));
        if (assetsIds != null && assetsIds.size() > 0) {
            assetsIds.forEach(mwAssetsIdsDTO -> {
                redisTemplate.delete(redisTemplate.keys(mwAssetsIdsDTO.getId() + "*"));
            });
        }
        return Reply.ok();
    }
}
