package cn.mw.monitor.model.param.prometheusContainer;

import cn.mw.monitor.model.param.PrometheusContainerEnum;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class PrometheusInstanceParam {
    private String app;

    @JSONField(name = "host_ip")
    private String hostIp;

    private String instance;

    private String job;

    private String node;

    private String pod;

    private String uid;

    private String namespace;

    private String container;

    @JSONField(name = "container_id")
    private String containerId;

    //实例名称
    private String instanceName;

    //监控服务器id
    private Integer monitorServerId;

    //数据类型
    private String type;

    //模型Id
    private Integer modelId;

    private String checkKey;

    private String esId;
    private String modelIndex;
    private Integer modelInstanceId;
}
