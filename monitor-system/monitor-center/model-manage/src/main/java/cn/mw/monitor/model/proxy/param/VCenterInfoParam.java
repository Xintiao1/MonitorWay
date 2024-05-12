package cn.mw.monitor.model.proxy.param;

public class VCenterInfoParam {
    private String url;
    private String userName;
    private String password;
    private String assetsId;
    private Integer monitorServerId;
    private String monitorServerName;

    public VCenterInfoParam(String url ,String userName ,String password ,String assetsId
            ,Integer monitorServerId ,String monitorServerName){
        this.url = url;
        this.userName = userName;
        this.password = password;
        this.assetsId = assetsId;
        this.monitorServerId = monitorServerId;
        this.monitorServerName = monitorServerName;
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

    public String getAssetsId() {
        return assetsId;
    }

    public void setAssetsId(String assetsId) {
        this.assetsId = assetsId;
    }

    public Integer getMonitorServerId() {
        return monitorServerId;
    }

    public void setMonitorServerId(Integer monitorServerId) {
        this.monitorServerId = monitorServerId;
    }

    public String getMonitorServerName() {
        return monitorServerName;
    }

    public void setMonitorServerName(String monitorServerName) {
        this.monitorServerName = monitorServerName;
    }
}
