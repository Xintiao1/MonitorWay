package cn.mw.monitor.util.entity;

import lombok.Data;

@Data
public class EmailFrom {

    private String hostName;
    private Integer port;
    private String username;
    private String password;
    private String personal;
    private Boolean isSsl;
    private String emailHeaderTitle;
    private String logo;
    private String url;
    private Boolean isLogo;
    private Boolean isDelsuffix;
    private String[] emailToCC;
}
