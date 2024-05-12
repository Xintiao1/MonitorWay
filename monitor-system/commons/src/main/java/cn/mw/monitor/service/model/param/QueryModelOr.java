package cn.mw.monitor.service.model.param;

import java.util.List;

public class QueryModelOr implements QueryModelParam {
    private ModelQueryType type;
    private String itemName;
    private List dataList;
    private Class dataType;

    public QueryModelOr(String itemName, List dataList) {
        this(dataList);
        this.itemName = itemName;
    }

    public QueryModelOr(List dataList){
        this.dataList = dataList;
        this.type = ModelQueryType.OR;
        this.dataType = dataList.get(0).getClass();
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public List getDataList() {
        return dataList;
    }

    public void setDataList(List dataList) {
        this.dataList = dataList;
    }

    public ModelQueryType getType() {
        return type;
    }

    public Class getDataType() {
        return dataType;
    }

    public String toString() {
        return "QueryModelOr{" +
                "type='" + type +
                ", itemName=" + itemName +
                ", dataList=" + dataList +
                ", dataType=" + dataType +
                '}';
    }
}
