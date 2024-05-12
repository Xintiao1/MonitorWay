package cn.mw.monitor.user.model;

import lombok.Data;

@Data
public class MwViewUserControlAction {
    private Integer userId;
    private String loginName;
    private String cond;
    private String operation;
}
