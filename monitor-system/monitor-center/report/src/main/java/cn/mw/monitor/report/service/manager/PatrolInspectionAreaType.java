package cn.mw.monitor.report.service.manager;

/**
 * 巡检报告区域类型枚举
 */
public enum PatrolInspectionAreaType {

    PATROL_INSPECTION_AREA(26, "核心网区域"),
    WORK_AREA(27, "办公网区域");

    private Integer type;
    private String name;

    PatrolInspectionAreaType(Integer type, String name) {
        this.type = type;
        this.name = name;
    }

    public Integer getType() {
        return type;
    }


    public String getName() {
        return name;
    }



}
