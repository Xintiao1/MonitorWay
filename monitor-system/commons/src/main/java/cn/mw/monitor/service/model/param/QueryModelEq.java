package cn.mw.monitor.service.model.param;

public class QueryModelEq implements QueryModelParam {
    private ModelQueryType type;
    private String itemName;
    private Object value;

    public QueryModelEq(String itemName, Object value) {
        this.type = ModelQueryType.Equal;
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
        return "QueryModelEq{" +
                "type='" + type +
                ", itemName=" + itemName +
                ", value=" + value +
                '}';
    }
}