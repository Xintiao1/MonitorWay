package cn.mw.monitor.screen.dto;

import lombok.Data;

/**
 * @author xhy
 * @date 2020/4/14 9:59
 */
@Data
public class HistEventDto {
    private String severity;
    private String name;
    private String time;
    //资产主键id
    private String id;
    private String ip;
    private String assetsName;
}
