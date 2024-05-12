package cn.mw.monitor.model.proxy.param;

import cn.mw.monitor.model.param.virtual.QueryVirtualInstanceParam;

import java.util.Map;

public class VCenterVirtualInstanceParam {
    private String url;
    private String userName;
    private String password;
    private QueryVirtualInstanceParam param;
    private Map<String, Integer> modelInstanceIdMap;

    public VCenterVirtualInstanceParam(String url , String userName , String password ,QueryVirtualInstanceParam param){
        this.url = url;
        this.userName = userName;
        this.password = password;
        this.param = param;
    }

    public VCenterVirtualInstanceParam(String url , String userName , String password ,QueryVirtualInstanceParam param ,Map<String, Integer> modelInstanceIdMap){
        this(url ,userName ,password ,param);
        this.modelInstanceIdMap = modelInstanceIdMap;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public QueryVirtualInstanceParam getParam() {
        return param;
    }

    public void setParam(QueryVirtualInstanceParam param) {
        this.param = param;
    }

    public Map<String, Integer> getModelInstanceIdMap() {
        return modelInstanceIdMap;
    }

    public void setModelInstanceIdMap(Map<String, Integer> modelInstanceIdMap) {
        this.modelInstanceIdMap = modelInstanceIdMap;
    }
}
