package cn.mw.monitor.model.param.superfusion;

import lombok.Data;

/**
 * @author qzg
 * @date 2023/8/2
 */
@Data
public class SuperFusionBaseClusterData {
    //主机总数
    private Integer hostNum;
    //离线数
    private Integer hostOffLine;
    //在线数
    private Integer hostOnLine;
    //虚拟机总数
    private Integer vmNum;
    //虚拟机未运行数量
    private Integer vmOffNum;
    //虚拟机运行中数量
    private Integer vmOnNum;
    private String ip;
    private String name;
    //CPU
    private String totalCpu;
    private String usedCpu;
    private String ratioCpu;
    //物理总内存
    private String totalMem;
    //物理已使用内存
    private String usedMem;
    //物理内存使用率
    private String ratioMem;
    //总存储
    private String totalStg;
    //已使用
    private String usedStg;
    //存储使用率
    private String ratioStg;
    //计算内存容量
    private String totalConf;
    //已配置
    private String usedConf;
    //配置内存比
    private String ratioConf;

}
