package cn.mw.monitor.visualized.enums;

/**
 * 中控大屏监控项枚举
 */
public enum VisualizedZkSoftWareItemEnum {

    //机柜信息
    MW_IOT_DOOR1_STATUS("MW_IOT_DOOR2_STATUS","前门","frontDoor"),
    MW_IOT_DOOR2_STATUS("MW_IOT_DOOR1_STATUS","后门","behindDoor"),
    MW_IOT_HUMIDITY1_VALUE("MW_IOT_HUMIDITY1_VALUE","湿度(上)","humidityUp"),
    MW_IOT_HUMIDITY2_VALUE("MW_IOT_HUMIDITY2_VALUE","湿度(下)","humidityDown"),
    MW_IOT_NOISE1_VALUE("MW_IOT_NOISE1_VALUE","噪声(上)","noiseUp"),
    MW_IOT_NOISE2_VALUE("MW_IOT_NOISE2_VALUE","噪声(下)","noiseDown"),
    MW_IOT_TEMPERTURE1_VALUE("MW_IOT_TEMPERTURE1_VALUE","温度(上)","temperatureUp"),
    MW_IOT_TEMPERTURE2_VALUE("MW_IOT_TEMPERTURE2_VALUE","温度(下)","temperatureDown"),
    //配电柜信息
    MW_OUTPUT_VOLTAGE("MW_OUTPUT_VOLTAGE","电压","voltage"),
    MW_OUTPUT_CURRENT("MW_OUTPUT_CURRENT","电流","current"),
    MW_AC_CURTAILMENT("MW_AC_CURTAILMENT","输出功率","curtailment"),
    MW_PDB_STATUS("MW_PDB_STATUS","配电柜定时均冲充","distributionPdb"),
    MW_PANEL_BATTERY1("MW_PANEL_BATTERY1","配电柜电池1","distributionBattery1"),
    MW_PANEL_REMAINDER("MW_PANEL_REMAINDER","配电柜剩余","distributionRemainder"),
    MW_PANEL_AC1_SINGLEPHASE_VOLTAGE("MW_PANEL_AC1_SINGLEPHASE_VOLTAGE","配电柜单相电压","distributionSinglephaseVoltage"),
    MW_LIMITING_POINT("MW_LIMITING_POINT","限流点","currentLimitingPoint"),
    MW_INPUT_VOLTAGE("MW_INPUT_VOLTAGE","输入电压","importVoltage"),
    MW_COMMUNICATION_STATUS("MW_COMMUNICATION_STATUS","交流状态","alternatingStatus"),
    MW_PANEL_ENERGY_STATUS("MW_PANEL_ENERGY_STATUS","节能状态","energyStatus"),
    MW_TEMPERATURE_LIMIT_POWER("MW_TEMPERATURE_LIMIT_POWER","温度限功率","temperatureLimitPower"),
    MW_PANEL_BATTERY_CHAREG("MW_PANEL_BATTERY_CHARGE","电池百分比","distributionPanelBatteryChareg"),
    ON_LINE("ResourceManager: Active NMs","在线节点数量","distributionPanelBatteryChareg"),
    PROCESS_HEALTH("PROCESS_HEALTH","进程状态","processStatus"),
    PROCESS_CPU_USAGE("PROCESS_CPU_USAGE","进程Cpu","processCpu"),
    PROCESS_MEM_USAGE("PROCESS_MEM_USAGE","进程内存","processMem"),
    ;

    private String name;
    private String desc;
    private String property;

    VisualizedZkSoftWareItemEnum( String name,String desc,String property) {
        this.name = name;
        this.desc = desc;
        this.property = property;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }


    public static String getProPerty(String name){
        VisualizedZkSoftWareItemEnum[] values = values();
        for (VisualizedZkSoftWareItemEnum itemEnum : values) {
            if(name.contains(itemEnum.getName())){
                return itemEnum.property;
            }
        }
        return null;
    }
}
