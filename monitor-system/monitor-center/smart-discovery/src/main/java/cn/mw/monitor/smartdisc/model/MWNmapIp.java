package cn.mw.monitor.smartdisc.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MWNmapIp {

    private Integer id;
    private String ip;
    private String ipType;
    private String osType;
    private String hostName;

    //任务开始时间
    private String startTime;

    //任务结束时间
    private String endTime;

    //扫描结果个数
    private Integer serviceCount;

}
