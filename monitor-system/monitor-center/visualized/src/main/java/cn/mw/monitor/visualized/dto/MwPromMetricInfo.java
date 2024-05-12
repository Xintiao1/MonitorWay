package cn.mw.monitor.visualized.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @ClassName
 * @Description Prometheus标签DTO
 * @Author gengjb
 * @Date 2023/6/7 14:02
 * @Version 1.0
 **/
@Data
@ApiModel("Prometheus标签DTO")
public class MwPromMetricInfo {

    //指标名称
    private String __name__;

    private String endpoint;

    private String host_ip;

    private String instance;

    private String job;

    private String namespace;

    //节点名称
    private String node;

    private String node_id;

    //pod名称
    private String pod;

}
