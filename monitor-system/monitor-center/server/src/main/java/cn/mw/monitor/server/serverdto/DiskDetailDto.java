package cn.mw.monitor.server.serverdto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

/**
 * @author xhy
 * @date 2020/4/28 16:55
 */
@Data
@Builder
public class DiskDetailDto {
    @ApiModelProperty("磁盘总容量")
    private String diskTotal;
    @ApiModelProperty("已用磁盘容量")
    private String diskUser;
    @ApiModelProperty("剩余磁盘容量")
    private String diskFree;
    @ApiModelProperty("磁盘利用率")
    private String diskUserRate;

    @ApiModelProperty("磁盘空闲容量百分比")
    private String diskFreeRate;
    @ApiModelProperty("磁盘每秒读操作的次数")
    private String diskReadCount;
    @ApiModelProperty("磁盘每秒写操作的次数")
    private String diskWriterCount;
    @ApiModelProperty("磁盘每秒读延时")
    private String diskReadDelay;
    @ApiModelProperty("磁盘每秒写延时")
    private String diskWriteDelay;
    @ApiModelProperty("磁盘每秒读取的字节数")
    private String diskReadByte;
    @ApiModelProperty("磁盘每秒写入的字节数")
    private String diskWriteByte;


    @ApiModelProperty("IOPS:磁盘每秒的读IO数量")
    private String diskWriteIOCount;
    @ApiModelProperty("IOPS:磁盘每秒的写IO数量")
    private String diskReadIOCount;
    @ApiModelProperty("磁盘每秒读IO延时")
    private String diskReadIODelay;
    @ApiModelProperty("磁盘每秒写IO延时")
    private String diskWriterIODelay;

    @ApiModelProperty("IOPS:磁盘每秒IO的吞吐量")
    private String disKIORate;
    @ApiModelProperty("磁盘处理每个IO的平均时间")
    private String diskIOAvgTime;

}
