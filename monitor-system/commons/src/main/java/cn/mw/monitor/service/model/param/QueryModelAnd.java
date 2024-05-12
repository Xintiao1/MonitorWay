package cn.mw.monitor.service.model.param;

import java.util.List;

public class QueryModelAnd implements QueryModelParam{
    private ModelQueryType type;
    private String itemName;
    private List dataList;
    private Class dataType;

    public QueryModelAnd(String itemName ,List dataList){
        this(dataList);
        this.itemName = itemName;
    }

    public QueryModelAnd(List dataList){
        this.dataList = dataList;
        this.type = ModelQueryType.AND;
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
        return "QueryModelAnd{" +
                "type='" + type +
                ", itemName=" + itemName +
                ", dataList=" + dataList +
                ", dataType=" + dataType +
                '}';
    }
}
