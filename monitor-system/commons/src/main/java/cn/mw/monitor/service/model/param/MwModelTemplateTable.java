package cn.mw.monitor.service.model.param;


import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class MwModelTemplateTable {
    private Integer id;

    private Integer port;

    private Integer monitorPort;

    private List<MwModelZabbixTemplateParam> template;

    private Integer groupId;

    private String systemObjid;

    private String description;

    private String brand;

    private String specification;

    private String modelGroupId;

    private List<Integer> modelGroupIdList;

    private String groupNodes;

    private String modelId;

    private Integer monitorMode;

    private String creator;

    private Date createDate;

    private String modifier;

    private Date modificationDate;

    /**
     * zabbix中对应模板所用的interfaces类型 /agent/JMX/IPMI/SNMP
     */
    private int interfacesType;

    private boolean status;
}
