package cn.mw.monitor.service.task;

import cn.mwpaas.common.enums.DateUnitEnum;
import cn.mwpaas.common.utils.DateUtils;
import lombok.Data;

import java.util.Date;

@Data
public class ServiceStatus {
    private Date visitedTime;
    private long visitInterval;

    public ServiceStatus(long visitInterval){
        this.visitedTime = new Date();
        this.visitInterval = visitInterval;
    }

    public synchronized void visit(){
        visitedTime = new Date();
    }

    public synchronized boolean isExceedInteval(){
        Date now = new Date();
        long interval = DateUtils.between(visitedTime ,now , DateUnitEnum.SECOND);
        if(interval > visitInterval){
            return true;
        }
        return false;
    }
}
