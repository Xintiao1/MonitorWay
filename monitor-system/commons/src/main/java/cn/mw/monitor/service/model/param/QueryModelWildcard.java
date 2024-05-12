package cn.mw.monitor.service.model.param;

public class QueryModelWildcard implements QueryModelParam {
    private ModelQueryType type;
    private String itemName;
    private Object value;

    public QueryModelWildcard(String itemName, Object value) {
        this.type = ModelQueryType.Wildcard;
        this.itemName = itemName;
        this.value = value;
    }

    public ModelQueryType getType() {
        return type;
    }

    public void setType(ModelQueryType type) {
        this.type = type;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String toString() {
        return "QueryModelExist{" +
                "type='" + type +
                ", itemName=" + itemName +
                ", value=" + value +
                '}';
    }
}