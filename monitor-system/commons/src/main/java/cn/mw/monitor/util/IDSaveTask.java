package cn.mw.monitor.util;

import java.util.TimerTask;

public class IDSaveTask extends TimerTask {
    private RedisUtils redisUtils;
    private String key;
    private ModuleIDManager moduleIDManager;

    public IDSaveTask(RedisUtils redisUtils ,String key ,ModuleIDManager moduleIDManager){
        this.redisUtils = redisUtils;
        this.key = key;
        this.moduleIDManager = moduleIDManager;
    }
    @Override
    public void run() {
        redisUtils.set(key ,moduleIDManager.getLastGenTime().get());
    }
}
