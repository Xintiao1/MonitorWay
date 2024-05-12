package cn.mw.monitor.prometheus.vo;

import lombok.Data;

import java.util.List;

@Data
public class PanelQueryParamVo {

    private Integer serviceId;

    private String query;

    private String start;

    private String end;

    private boolean isQueryRange;

    private List<String> namespaceList;

    private List<String> hostIpList;

    private List<String> nodeList;

    private List<String> containerList;
}
