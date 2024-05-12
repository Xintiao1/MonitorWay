package cn.mw.monitor.model.type;

/**
 * @author guiquanwnag
 * @datetime 2023/6/30
 * @Description 需要查看的视图表格内容
 */
public enum TableViewEnum {
    ARP(1, "" ,0),
    MAC(2, "", 1),
    INTERFACE(3, "",2);

    /**
     * 类别ID（用于前端筛选）
     */
    private Integer typeId;

    /**
     * 描述
     */
    private String desc;

    /**
     * 设备返回list的index
     */
    private Integer index;


    TableViewEnum(Integer typeId, String desc, Integer index) {
        this.typeId = typeId;
        this.desc = desc;
        this.index = index;
    }

    public Integer getTypeId() {
        return typeId;
    }

    public Integer getIndex() {
        return index;
    }

    public static TableViewEnum getByTypeId(int typeId) {
        for (TableViewEnum view : values()) {
            if (view.getTypeId().equals(typeId)) {
                return view;
            }
        }
        return null;
    }
}
