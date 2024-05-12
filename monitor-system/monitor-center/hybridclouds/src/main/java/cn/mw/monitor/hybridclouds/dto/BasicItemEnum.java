package cn.mw.monitor.hybridclouds.dto;

/**
 * @author qzg
 * @Date 2021/6/6
 */
public enum BasicItemEnum {
    HC_BPSRead("BPSRead","BPS读取速率"),
    HC_CPU_CORE("CPU_CORES","CPU核数"),
    HC_InstanceName("InstanceName","实例名称"),
    HC_Memory("Memory","内存"),
    HC_OSName("OSName","OS名称"),
    HC_IntranetBandwidth("IntranetBandwidth","内网带宽"),
    HC_IntranetRX("IntranetRX","接收速率"),
    HC_IntranetTX("IntranetTX","发送速率"),
    HC_IOPSRead("IOPSRead","IOPS速率"),
    HC_BPSWrite("BPSWrite","BPS写入速率");

    private String name;
    private String chName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getChName() {
        return chName;
    }

    public void setChName(String chName) {
        this.chName = chName;
    }

    BasicItemEnum(String name, String chName) {
        this.name = name;
        this.chName = chName;
    }

}
