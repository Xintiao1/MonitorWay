package cn.mw.monitor.service.user.model;

import lombok.Data;

/**
 * @description:TODO
 * @author:zy.quaee
 * @date:2020/12/21 10:33
 */
@Data
public class MwUserControlSetting {

    private Integer userId;
    private String loginName;
    private MWTime mwTime;
    private MWIp mwIp;
    private MWMac mwMac;
//    private String conditionsValue;
//    private String actionValue;
}
