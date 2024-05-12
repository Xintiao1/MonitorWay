package cn.mw.monitor.assets.service.impl;

import cn.mw.monitor.service.assets.api.MwInspectModeService;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author gengjb
 * @description 猫维检查模式接口实现
 * @date 2023/7/19 9:27
 */
@Service
@Slf4j
public class MwInspectModeServiceImpl implements MwInspectModeService {


    @Autowired
    private StringRedisTemplate redisTemplate;

    private final String redisKey = "mwInspectMode";

    /**
     * 开启或者关闭检查模式
     * @param enable  是否启用
     * @return
     */
    @Override
    public Reply openOrCloseInspectMode(boolean enable) {
        try {
            //将数据永久存入redis
            redisTemplate.delete(redisKey);
            redisTemplate.opsForValue().set(redisKey, String.valueOf(enable));
            return Reply.ok("模式设置成功");
        }catch (Throwable e){
            log.error("MwInspectModeServiceImpl {} openOrCloseInspectMode()",e);
            return Reply.fail("MwInspectModeServiceImpl {} openOrCloseInspectMode()"+e.getMessage());
        }
    }

    /**
     * 获取猫维是否开启检查模式的值
     * @return
     */
    @Override
    public boolean getInspectModeInfo() {
        try {
            //获取redis数据
            String redisValue = redisTemplate.opsForValue().get(redisKey);
            if(StringUtils.isBlank(redisValue)){return false;}
            return Boolean.parseBoolean(redisValue);
        }catch (Throwable e){
            log.error("MwInspectModeServiceImpl {} getInspectModeInfo()",e);
            return false;
        }
    }
}
