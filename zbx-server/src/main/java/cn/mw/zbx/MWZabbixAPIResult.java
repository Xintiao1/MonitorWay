package cn.mw.zbx;

import cn.mw.monitor.service.runtimeCache.CacheData;
import lombok.Data;

import java.util.Date;

@Data
public class MWZabbixAPIResult extends CacheData {
    private long creatTime;
    //缓存间隔时间,秒
    private int interval = 20;

    public int code;

    public String message;

    public Object data;

    public MWZabbixAPIResult() {
        this.data = new Object();
        this.creatTime = new Date().getTime();
    }

    public boolean isFail() {
        if (code != MWZabbixAPIResultCode.SUCCESS.code()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isRemoveAble(long now) {
        long diff = now - creatTime;
        if(diff > 1000 * interval){
            return true;
        }
        return false;
    }
}
