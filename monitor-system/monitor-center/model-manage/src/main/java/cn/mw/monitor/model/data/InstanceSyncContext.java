package cn.mw.monitor.model.data;

import cn.mw.monitor.model.param.AddAndUpdateModelInstanceParam;

public class InstanceSyncContext {
    private AddAndUpdateModelInstanceParam param;

    public InstanceSyncContext(AddAndUpdateModelInstanceParam param){
        this.param = param;
    }

    public AddAndUpdateModelInstanceParam getParam() {
        return param;
    }

    public void setParam(AddAndUpdateModelInstanceParam param) {
        this.param = param;
    }
}
