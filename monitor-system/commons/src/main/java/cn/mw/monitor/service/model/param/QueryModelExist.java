package cn.mw.monitor.service.model.param;

public class QueryModelExist implements QueryModelParam{
    private ModelQueryType type;
    private String itemName;

    public QueryModelExist(String itemName){
        this.type = ModelQueryType.Exist;
        this.itemName = itemName;
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

    public String toString() {
        return "QueryModelExist{" +
                "type='" + type +
                ", itemName=" + itemName +
                '}';
    }
}
