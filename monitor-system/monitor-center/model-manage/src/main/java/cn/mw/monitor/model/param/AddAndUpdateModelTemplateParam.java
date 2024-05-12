package cn.mw.monitor.model.param;

import cn.mw.monitor.bean.BaseParam;
import cn.mw.monitor.service.model.param.MwModelZabbixTemplateParam;
import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date
 */
@Data
public class AddAndUpdateModelTemplateParam extends BaseParam {
    private Integer id;

    private List<Integer> idList;
    /**
     * 模板名称
     */
    private List<MwModelZabbixTemplateParam> template;

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
     * 模型分组（资产类型）
     */
    private Integer modelGroupId;

    /**
     * 多级模型分组
     */
    private List<Integer> modelGroupIdList;

    /**
     * 模型（资产子类型）
     */
    private Integer modelId;

    /**
     * 监控协议
     */
    private Integer monitorMode;

    private List<Integer> monitorModeList;

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
