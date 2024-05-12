package cn.mw.monitor.virtualization.dto;

/**
 * @author syt
 * @Date 2020/7/8 11:20
 * @Version 1.0
 */
public enum BasicItemEnum {

    version("MWVM_VERSION","版本号"),
    versionName("VMWARE_NAME","版本全称"),
    VMWARE_EVENT_LOG("VMWARE_EVENT_LOG","VMWARE事件日志"),
    DATACENTER_NAME("DATACENTER_NAME","数据中心"),
    VMs("Number of guest VMs","虚拟机"),
    status("Overall status","主机状态"),
    HARDWARE("HOST_MODEL","型号"),
    MWVM_CPU_MODEL("MWVM_CPU_MODEL","处理器类型"),
    MWVM_CPU_THREADS("MWVM_CPU_THREADS","逻辑处理器"),
    MWVM_UPTIME("MWVM_UPTIME","正常运行时间"),
    MWVM_CPU_CORES("MWVM_CPU_CORES","CPU核数"),
    VM_POWER_STATE("VM_POWER_STATE","虚拟机状态"),
    VM_VMHOST_NAME("VM_VMHOST_NAME","宿主机"),

    ;

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
