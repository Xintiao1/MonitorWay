package cn.mw.monitor.scan.dataview;

import cn.mw.monitor.api.common.Constants;
import cn.mw.monitor.snmp.model.ExceuteInfo;
import cn.mw.monitor.util.AssetsUtils;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
public class ScanProcView {
    private Integer scanruleId;
    private int processCount;
    private float eiProcessCount;
    private float allCount;
    private float allDBCount;
    private float insertCount;
    private Date startTime;
    private Date endTime;
    private String timeLength;
    private boolean isFinish;

    public void setExceuteInfo(ExceuteInfo ei){
        synchronized (ei) {
            this.scanruleId = ei.getScanruleId();
            this.eiProcessCount = ei.getProcessingCount();
            this.allCount = ei.getAllCount();
            this.allDBCount = ei.getAllDBcount();
            this.insertCount = ei.getInsertCount().get();
            this.startTime = ei.getStartTime();
            this.endTime = ei.getEndTime();
            this.isFinish = false;

            if (ei.isScanDoned()) {
                this.processCount = 100;
            } else {
                this.processCount = floatToInt((this.eiProcessCount / this.allCount) * 80);
                if(0 !=ei.getAllDBcount()){
                    float dbPercent = this.insertCount / this.allDBCount;
                    this.processCount = this.processCount + floatToInt(dbPercent * 20);
                }
            }

            this.timeLength = AssetsUtils.scanRuleTime(this.startTime, this.endTime);
        }
    }

    private int floatToInt(float f){
        int i = 0;
        if(f>0) //正数
        {
            i = (int)(f*10 + 5)/10;
        }
        else if(f<0) //负数
        {
            i =  (int)(f*10 - 5)/10;
        }
        else {
            i = 0;
        }
        return i;
    }
}
