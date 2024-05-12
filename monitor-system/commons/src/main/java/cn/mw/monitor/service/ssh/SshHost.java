package cn.mw.monitor.service.ssh;

import java.util.Objects;

public class SshHost {
    private String loginName;
    private String user;
    private String host;
    private String passwd;
    private int port;

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SshHost sshHost = (SshHost) o;
        return port == sshHost.port &&
                Objects.equals(loginName, sshHost.loginName) &&
                Objects.equals(user, sshHost.user) &&
                Objects.equals(host, sshHost.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(loginName, user, host, port);
    }
}
