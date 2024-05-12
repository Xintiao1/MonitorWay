package cn.mw.monitor.hybridclouds.dto;

/**
 * @author qzg
 * @Date 2021/6/6
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
    MW_DISK_USED("MW_DISK_USED");


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
