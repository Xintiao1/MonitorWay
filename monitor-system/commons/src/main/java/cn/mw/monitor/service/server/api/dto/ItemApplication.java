package cn.mw.monitor.service.server.api.dto;

import lombok.Data;

/**
 * @author xhy
 * @date 2020/4/29 22:54
 */
@Data
public class ItemApplication {
    private String itemid;
    private String name;
    private String chName;
    private String application;
    private String delay;
    private Long sortDelay;

//    private Double sortDelay;
    private Long sortLastclock;

    private String lastvalue;
    private String lastclock;

    private String units;
//    0：数字浮点；1：字符；2：日志；3：数字无符号；4：文字
    private String value_type;
    /**
     * 这个字段相当于item中的value_type
     */
    private int history;
//    0：正常；1：不支持
    private String state;
//    value值映射关系的valuemapid
    private String valuemapid;
    //分区名称
    private String typeName;
    //自动发现规则id
    private String discoverId;


    private Double doubleValue;

    private String hostid;
}
