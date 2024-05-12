package cn.mw.monitor.user.model;

import lombok.Data;

@Data
public class MwViewUserControl {
    private Integer userId;
    private String loginName;
    private String rule;
    private String controlName;
}
