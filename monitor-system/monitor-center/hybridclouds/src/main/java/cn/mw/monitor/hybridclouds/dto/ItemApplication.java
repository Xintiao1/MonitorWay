package cn.mw.monitor.hybridclouds.dto;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author qzg
 * @date 2021/6/8
 */
@Data
@Slf4j
public class ItemApplication {
    private String hostid;
    private String itemid;
    private String name;
    private String chName;
    private String application;
    private String delay;
    private Double lastvalue;
    private String stringValue;
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

    private List<GroupHosts> hosts;
    private List<InterfaceDTO> interfaces;

    public void setName(String name) {
        this.name = name.indexOf("[") != -1 ? name.substring(name.indexOf("]") + 1) : name;
    }

    public void setLastvalue(String lastvalue) {
        try {
            this.lastvalue = Double.parseDouble(lastvalue);
        } catch (Exception e) {
//            log.error("String to Double error lastvalue:{}", lastvalue, e);
            this.lastvalue = null;
            this.stringValue = lastvalue;
        }
    }
}
