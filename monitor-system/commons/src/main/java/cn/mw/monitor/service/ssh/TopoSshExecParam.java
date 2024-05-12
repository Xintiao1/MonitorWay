package cn.mw.monitor.service.ssh;

import lombok.Data;

@Data
public class TopoSshExecParam {
    private String loginName;
    private String host;
    private String user;
    private String passwd;
    private String command;
    private Integer port;
    private Integer assetTypeId;
    private  Integer quickCmd = 0;



    public SshHost tranform(){
        SshHost sshHost = new SshHost();
        sshHost.setLoginName(loginName);
        sshHost.setHost(host);
        sshHost.setUser(user);
        sshHost.setPasswd(passwd);

        if(null != port){
            sshHost.setPort(port);
        }else{
            sshHost.setPort(22);
        }

        return sshHost;
    }
}
