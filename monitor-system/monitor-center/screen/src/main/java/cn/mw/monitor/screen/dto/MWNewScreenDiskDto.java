package cn.mw.monitor.screen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName MWNewScreenDiskDto
 * @Description 新大屏磁盘信息参数
 * @Author gengjb
 * @Date 2022/1/11 16:07
 * @Version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MWNewScreenDiskDto {

    //资产HostID
    private String assetsId;

    //资产ID
    private String id;

    //资产状态
    private String assetsStatus;

    //资产IP
    private String ip;

    //分区名称
    private String type;

    //资产名称
    private String name;

    //zabbix服务器ID
    private Integer monitorServerId;

    //磁盘使用率
    private String diskUtilizaTionRate;

    //磁盘总容量
    private String diskTotal;

    //磁盘已使用容量
    private String diskUsed;

    //磁盘未使用容量
    private String diskNotUsed;

}
