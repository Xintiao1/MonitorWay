package cn.mw.monitor.service.rule.param;

import lombok.Data;

import java.util.List;

@Data
public class ConstituentElement {
    private String condition;
    private int deep;
    private String key;
    private String parentKey;
    private String name;
    private String relation;
    private String value;
    private List<ConstituentElement> constituentElements;
}
