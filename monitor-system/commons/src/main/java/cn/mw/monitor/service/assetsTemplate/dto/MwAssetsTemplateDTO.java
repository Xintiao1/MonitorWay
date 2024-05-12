package cn.mw.monitor.service.assetsTemplate.dto;


import cn.mw.monitor.service.assetsTemplate.model.MwAssetsTemplateTable;
import lombok.Data;

import java.util.List;

/**
 * @author baochengbin
 * @date 2020/4/6
 */
@Data
public class MwAssetsTemplateDTO extends MwAssetsTemplateTable {
    private String assetsTypeName;

    private String subAssetsTypeName;

    private String monitorModeName;

    private String templateId;

    private String templateName;

    private String monitorServerId;

    private String monitorServerName;

    /**
     * zabbix中对应模板所用的interfaces类型 /agent/JMX/IPMI/SNMP
     */
    private int interfacesType;

    //系统OID
    private String systemObjid;
//  关联资产的数量
    private List<String> assetsIds;

    //设备描述信息,在模版匹配的时候, 由于字符集会导致描述信息乱码
    //当使用正确的编码匹配完成之后，会把正确的编码的字符串设置到这里
    private String sysDesc;

    private int typeId;

}
