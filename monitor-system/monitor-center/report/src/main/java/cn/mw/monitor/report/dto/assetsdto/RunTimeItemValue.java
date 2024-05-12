package cn.mw.monitor.report.dto.assetsdto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Data
public class RunTimeItemValue implements Comparator<RunTimeItemValue> {
    private String assetName;
    private String ip;
    private String itemName;
    private String maxValue;
    private String minValue;
    private String avgValue;
    private String interfaceName;
    private String diskName;
    private String assetUtilization;
    private String outInterfaceAvgValue;
    private Double sortLastAvgValue;
    private String itemId;

    private String cpuUseSize;

    private String cpuTotalSize;

    private String cpuUtilizationRate;

    private String memoryUtilizationRate;

    @ApiModelProperty("磁盘分区名称")
    private String diskType;
    @ApiModelProperty("磁盘总容量")
    private String diskTotal;
    @ApiModelProperty("磁盘已用容量")
    private String diskUser;
    @ApiModelProperty("磁盘剩余容量")
    private String diskFree;
    @ApiModelProperty("磁盘使用率")
    private String diskUserRate;
    @ApiModelProperty("磁盘可用率")
    private String diskUsableRate;
    @ApiModelProperty("磁盘更新时间")
    private String updateTime;

    private Set<String> itemIds;

    private String hostId;

    private String maxMemoryUtilizationRate;

    private String minMemoryUtilizationRate;

    /**
     * 类型分类  0：cpu,内存,丢包率  1：接口，磁盘  2：接口，磁盘
     */
    private Integer type;

    private String assetsId;

    private Integer serverId;

    private String name;

    private Date saveTime;

    //自动进程成功标识
    private boolean updateSuccess;

    private String time;

    //ping延迟
    private String icmpPing;

    //ICMP响应时间
    private String icmpResponseTime;

    //品牌
    private String brand;

    //品牌图标地址
    private String url;

    //位置
    private String location;

    private Integer urlType;

    //CPU是否需要显示颜色
    private Boolean isCpuColor;

    //内存是否需要显示颜色
    private Boolean isMemoryColor;

    private String maxValueTime;

    private String minValueTime;

    private String memoryMaxValueTime;

    private String memoryMinValueTime;

    private String assetsTypeName;


    @Override
    public int compare(RunTimeItemValue o1, RunTimeItemValue o2) {
        return -Double.compare(o1.getSortLastAvgValue(),o2.getSortLastAvgValue());
    }
}
