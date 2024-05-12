package cn.mw.monitor.service.activiti.param;

public class BaseProcessParam {
    //启动流程前参数
    private String modelName;
    private String paramClass;

    //流程引擎传递参数
    private boolean intercepted = true;
    private String instancdId;

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getParamClass() {
        return paramClass;
    }

    public void setParamClass(String paramClass) {
        this.paramClass = paramClass;
    }

    public boolean isIntercepted() {
        return intercepted;
    }

    public void setIntercepted(boolean intercepted) {
        this.intercepted = intercepted;
    }

    public String getInstancdId() {
        return instancdId;
    }

    public void setInstancdId(String instancdId) {
        this.instancdId = instancdId;
    }
}
