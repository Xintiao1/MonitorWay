package cn.mw.monitor.model.param;

/**
 * 模型管理操作类型
 */
public enum OperationTypeEnum {
    CREATE_MODEL("新增模型"),
    EDITOR_MODEL("编辑模型"),
    DELETE_MODEL("删除模型"),
    CREATE_MODEL_GROUP("新增模型分类"),
    EDITOR_MODEL_GROUP("编辑模型分类"),
    DELETE_MODEL_GROUP("删除模型分类"),
    CREATE_PROPERTIES("新增模型属性"),
    EDITOR_PROPERTIES("编辑模型属性"),
    DELETE_PROPERTIES("删除模型属性"),
    CREATE_RELATION("新增模型关系"),
    EDITOR_RELATION("编辑模型关系"),
    DELETE_RELATION("删除模型关系"),
    CREATE_RELATION_GROUP("新增模型关系分组"),
    EDITOR_RELATION_GROUP("编辑模型关系分组"),
    DELETE_RELATION_GROUP("删除模型关系分组"),
    CREATE_INSTANCE("新增模型实例"),
    BATCH_MANAGE_INSTANCE("批量纳管实例"),
    EDITOR_INSTANCE("编辑模型实例"),
    CHANGE_INSTANCE("资产变更告警"),
    DELETE_INSTANCE("删除模型实例"),
    SHIFT_INSTANCE("转移模型实例"),
    CREATE_INSTANCE_RELATION("新增实例关系"),
    EDITOR_INSTANCE_RELATION("编辑实例关系"),
    DELETE_INSTANCE_RELATION("删除实例关系"),
    SYNC_INTERFACE("同步接口信息");
    private String name;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    OperationTypeEnum(String name) {
        this.name = name;
    }

}
