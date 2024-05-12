package cn.mw.monitor.service.assetsTemplate.dto;

import lombok.Data;

/**
 * @author baochengbin
 * @date 2020/5/9
 */
@Data
public class MwZabbixTemplateDTO {

    private Integer id;
    /*
     * 模版表id
     */
    private Integer assetstemplateId;
    /*
     * zabbix的模版id
     */
    private Integer templateId;

    /*
     * zabbix服务器id
     */
    private Integer serverId;
}
