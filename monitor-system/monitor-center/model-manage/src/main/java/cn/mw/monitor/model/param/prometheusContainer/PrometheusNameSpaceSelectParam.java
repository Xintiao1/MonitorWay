package cn.mw.monitor.model.param.prometheusContainer;

import lombok.Data;

import java.util.List;

@Data
public class PrometheusNameSpaceSelectParam {
    private String app;

    private String hostIp;

    private String instance;

    private String job;

    private String node;

    private String pod;

    private String uid;

    private String namespace;

    //实例名称
    private String instanceName;
    //实例Id
    private Integer modelInstanceId;

    private Integer monitorServerId;
    private List<PrometheusPodSelectParam> podList;
}
