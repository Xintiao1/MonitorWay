package cn.mw.monitor.assetsTemplate.api.param.assetsTemplate;

import cn.mw.monitor.service.assetsTemplate.dto.MwZabbixTemplateDTO;
import lombok.Data;

import java.util.List;

/**
 * @author baochengbin
 * @date
 */
@Data
public class AddAssetsTemplateParam{
    private Integer id;
    /**
     * 模板名称
     */
    private List<MwZabbixTemplateDTO> template;

    private String groupId;

    private String systemObjid;

    /**
     * 描述
     */
    private String description;

    /**
     * 品牌
     */
    private String brand;

    /**
     * 规格型号
     */
    private String specification;

    /**
     * 资产类型
     */
    private Integer assetsTypeId;

    /**
     * 资产子类型
     */
    private Integer subAssetsTypeId;

    /**
     * 监控协议
     */
    private Integer monitorMode;

    private String creator;

    private String modifier;

    private Integer monitorServerId;
    /**
     * 新增的模板名称
     */
    private String templateName;
    /**
     * zabbix中对应模板所用的interfaces类型 /agent/JMX/IPMI/SNMP
     */
    private int interfacesType;
}
