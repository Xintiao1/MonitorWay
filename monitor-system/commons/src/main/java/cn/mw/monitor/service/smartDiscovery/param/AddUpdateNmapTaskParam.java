package cn.mw.monitor.service.smartDiscovery.param;

import lombok.Data;

@Data
public class AddUpdateNmapTaskParam {
    //任务id
    private Integer id;
    //任务类型
    private Integer taskType;
    //任务名称
    private String taskName;
    //探测目标
    private String detectTarget;
    //节点组
    private String nodeGroupKey;

    private String detectTargetInput;
    private Boolean isSavedNode;
    //ip存活探测
    private Boolean ipLiveDetect;
    private Integer portGroupType;
    //TCP端口
    private String tcpPortGroup;
    //UDP端口
    private String udpPortGroup;
    private String portGroupKey;
    private Boolean isSavedPort;
    private String detectLiveNodeGroup;
    private Boolean isFingerScan;
    //脆弱性检测
    private Boolean frailDetect;
    private String fingerDetectNodeGroup;
    private String cycleRunValue;
    //
    private Integer cycleRunUnit;
    //执行方式
    private String runWay;
    private Integer exceptionIP;
    private String exceptionIPInput;
    //
    private String exceptionIPKey;
    //存为例外IP组
    private Boolean isSavedExceptionIPGroup;
    //勾选私网地址添加到例外IP中
    private Boolean isAddExceptionIP;
    //探测速度
    private Integer detectSpeed;
    //等待时间
    private String waitTime;
    //间隔时间
    private String intervals;
    //重试次数
    private String retryCount;
    //保存非存活数据
    private Boolean isSavedNonLiveData;

    //节点组名称
    private String nodeName;
    //端口组名称
    private String portName;
    //存活探测点组名称
    private String liveDetectNodeName;
    //指纹探测点组名
    private String fingerDetectName;
    //例外IP组名
    private String exceptionIPName;

    //自定义开始时间
    private String customStartTime;
}
