package cn.mw.monitor.service.server.api.dto;

import lombok.Data;

/**
 * @author gengjb
 * @description 用于接收zabbix返回的字符串值
 * @date 2023/7/13 16:36
 */
@Data
public class MwProcessHistoryDto {

    private String itemid;
    private String clock;
    private String value;
    private String ns;
    private String lastValue;
}
