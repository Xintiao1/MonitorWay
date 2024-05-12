package cn.mw.monitor.virtualization.dto;

/**
 * @author syt
 * @Date 2020/7/1 9:36
 * @Version 1.0
 */
public enum VmHostEnum {
    MWVM_CPU_CORES("MWVM_CPU_CORES"),
    MWVM_CPU_FREQUENCY("MWVM_CPU_FREQUENCY"),
    MWVM_CPU_MODEL("MWVM_CPU_MODEL"),
    MWVM_CPU_THREADS("MWVM_CPU_THREADS"),
    MWVM_CPU_USAGE("MWVM_CPU_USAGE"),
    MWVM_CLUSTER_NAME("MWVM_CLUSTER_NAME"),
    MWVM_UPTIME("MWVM_UPTIME"),
    MWVM_VERSION("MWVM_VERSION"),
    MWVM_MEMORY_TOTAL("MWVM_MEMORY_TOTAL"),
    MWVM_MEMORY_USAGE("MWVM_MEMORY_USAGE"),
    MWVM_MEMORY_USED("MWVM_MEMORY_USED"),
    MW_DISK_TOTAL("MW_DISK_TOTAL"),
    MW_DISK_USED("MW_DISK_USED"),
    VM_POWER_STATE("VM_POWER_STATE"),
    HOST_MODEL("HOST_MODEL"),
    HOST_VENDOR("HOST_VENDOR"),
    OVERALL_STATUS("Overall status");


    private String itemName;


    VmHostEnum(String itemName) {
        this.itemName = itemName;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
}
