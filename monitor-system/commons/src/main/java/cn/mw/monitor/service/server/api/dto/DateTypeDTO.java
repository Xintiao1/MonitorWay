package cn.mw.monitor.service.server.api.dto;

import lombok.Data;

/**
 * @author syt
 * @Date 2020/11/26 16:03
 * @Version 1.0
 */
@Data
public class DateTypeDTO {

    //  zabbix的history  查询条件 开始时间
    private Long startTime;
    //  zabbix的history  查询条件 结束时间
    private Long endTime;
    //   取数据的方式 ：false==>从zabbix中查 ； true==>从redisz中查
    private Boolean flag;
    //   redis的key值前缀
    private String keyPrefix;

}
