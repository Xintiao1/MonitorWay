package cn.mw.monitor.snmp.model;

import lombok.Data;
import lombok.ToString;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Data
@ToString
public class ExceuteInfo {
    private Object lockObject;
    private AtomicInteger successCount = new AtomicInteger(0);
    private AtomicInteger errorCount = new AtomicInteger(0);
    private Integer scanruleId;
    private Date startTime;
    private Date endTime;

    private int allCount = 0;
    private AtomicBoolean isScanDoned = new AtomicBoolean(false);
    private int allDBcount;
    private AtomicInteger insertCount = new AtomicInteger(0);

    public boolean isScanDoned() {
        return isScanDoned.get();
    }

    public void isScanDoned(boolean isDoned) {
        isScanDoned.set(isDoned);
    }

    public int getProcessingCount(){
        return successCount.get() + errorCount.get();
    }
}
