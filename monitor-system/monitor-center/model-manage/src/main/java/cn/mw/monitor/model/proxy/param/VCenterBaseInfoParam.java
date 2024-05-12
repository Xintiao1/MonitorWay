package cn.mw.monitor.model.proxy.param;

public class VCenterBaseInfoParam {
    private String url;
    private String userName;
    private String password;
    private String type;
    private String name;

    public VCenterBaseInfoParam(String url ,String userName ,String password ,String type ,String name){
        this.url = url;
        this.userName = userName;
        this.password = password;
        this.type = type;
        this.name = name;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
