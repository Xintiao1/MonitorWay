package cn.mw.monitor.assets.dto;

import lombok.Data;

import java.util.Date;

/**
 * 对表TangibleAssets数据新增
 * @author qzg
 * @Version 1.0
 */
@Data
public class MwAddAndUpdateTangibleAssetsTable {
    private String id;
    private String assetsId;//资产id
    private String assetsName;//资产名称
    private String hostName;//主机名称
    private String inBandIp;//带内ip
    private String outBandIp;//
    private Integer assetsTypeId;//资产类型
    private Integer assetsTypeSubId;//资产类型子分类
    private String pollingEngine;//轮询引擎
    private Integer monitorMode;//'监控方式'
    private String manufacturer;//'厂商'
    private String specifications;//'规格型号'
    private String description;//'描述'
    private String enable;//'资产状态'
    private Integer deleteFlag;//'删除标识符'
    private Integer monitorFlag;//'启动监控状态'
    private Integer settingFlag;//'启动配置状态'
    private String creator;//'创建人'
    private Date createDate;//'创建时间'
    private String modifier;//'修改人'
    private Date modificationDate;//'修改时间'
    private Integer scanSuccessId;//'扫描成功表id'
    private Integer monitorServerId;//'监控服务器id'
    private Integer snmpLev;//
    private String timing;//'定时间隔
    private String tpServerHostName;//'第三方监控服务器中主机名称'
    private String templateId;//'第三方监控服务器中关联模板id'
}
