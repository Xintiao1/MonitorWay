package cn.mw.monitor.service.assetsTemplate.model;


import cn.mw.monitor.service.assetsTemplate.dto.MwZabbixTemplateDTO;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class MwAssetsTemplateTable {
    private Integer id;

    private Integer port;

    private Integer monitorPort;

    private List<MwZabbixTemplateDTO> template;

    private String groupId;

    private String systemObjid;

    private String description;

    private String brand;

    private String specification;

    private Integer assetsTypeId;

    private Integer subAssetsTypeId;

    private Integer monitorMode;

    private String creator;

    private Date createDate;

    private String modifier;

    private Date modificationDate;

    /**
     * zabbix中对应模板所用的interfaces类型 /agent/JMX/IPMI/SNMP
     */
    private int interfacesType;
  }
