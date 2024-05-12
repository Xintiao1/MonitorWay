package cn.mw.monitor.screen.dto;

import lombok.Data;


/**
 * @author xhy
 * @date 2020/4/13 15:46
 */
@Data
public class AlertPriorityType {
    private String groupName;//主机组名称
    private String groupid;
    private Integer sum;
    private Integer count;
    private Integer count2;
    private Integer count3;
    private Integer count4;
    private Integer count5;
}
