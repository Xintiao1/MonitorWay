package cn.mw.monitor.service.scan.param;

import lombok.Data;

@Data
public class SearchParam {
    private String proxyServerIp;
    private String ip;
    private RuleParam ruleParam;
}
