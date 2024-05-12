package cn.mw.monitor.service.alert.callback;

import cn.mw.monitor.state.DataType;

import java.util.List;

public class BusinessIds {
    private DataType dataType;
    private List<String> businessIds;

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public List<String> getBusinessIds() {
        return businessIds;
    }

    public void setBusinessIds(List<String> businessIds) {
        this.businessIds = businessIds;
    }
}
