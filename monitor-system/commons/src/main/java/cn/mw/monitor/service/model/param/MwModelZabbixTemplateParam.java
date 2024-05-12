package cn.mw.monitor.service.model.param;

import lombok.Data;

/**
 * @author qzg
 * @date 2022/05/05
 */
@Data
public class MwModelZabbixTemplateParam {

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
