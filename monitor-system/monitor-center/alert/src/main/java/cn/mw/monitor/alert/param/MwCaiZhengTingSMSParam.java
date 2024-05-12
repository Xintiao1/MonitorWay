package cn.mw.monitor.alert.param;

import lombok.Data;

@Data
public class MwCaiZhengTingSMSParam {

    private String ruleId;

    private String appID;

    private String appKey;

    private String sign;

    private Integer type;

    private String account;

    private String password;

    private int id;

    private String url;

}
