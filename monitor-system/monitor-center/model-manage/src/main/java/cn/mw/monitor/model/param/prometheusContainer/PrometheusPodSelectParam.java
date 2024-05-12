package cn.mw.monitor.model.param.prometheusContainer;

import lombok.Data;

@Data
public class PrometheusPodSelectParam {
    private String app;

    private String hostIp;

    private String instance;

    private String job;

    private String node;

    private String pod;

    private String uid;

    private String namespace;

    private String container;

    private String containerId;

    //实例名称
    private String instanceName;
    //实例Id
    private Integer modelInstanceId;

    private Integer monitorServerId;

}
