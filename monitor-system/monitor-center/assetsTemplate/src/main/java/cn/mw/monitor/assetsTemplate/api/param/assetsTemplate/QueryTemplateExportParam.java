package cn.mw.monitor.assetsTemplate.api.param.assetsTemplate;

import cn.mw.monitor.bean.BaseParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

/**
 * @author qzg
 * @date 2021/7/5
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryTemplateExportParam extends BaseParam {

    private String assetsTypeName;

    private String subAssetsTypeName;

    private Integer id;
    /**
     * 模板名称
     */
    private String templateName;

    private String systemObjid;

    /**
     * zabbix中对应模板所用的interfaces类型 /agent/JMX/IPMI/SNMP
     */
    private int interfacesType;

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

    private String creator;

    private Date createDateStart;

    private Date createDateEnd;

    private String modifier;

    private Date modificationDateStart;

    private Date modificationDateEnd;

    private Map map;//字段名map
}
