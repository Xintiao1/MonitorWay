package cn.mw.monitor.configmanage.entity;


import cn.mw.monitor.bean.BaseParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * @author bkc
 * @date 2020/9/7
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QueryTangAssetsParam extends BaseParam {

    private String id;

    private String labelName;

    private String assetsId;

    private String inBandIp;
    /**
     * 资产名称
     */
    private String assetsName;

    /**
     * 主机名称
     */
    private String hostName;
    /**
     * 带外IP
     */
    private String outBandIp;

    /**
     * 资产类型
     */
    private Integer assetsTypeId;

    /**
     * 资产子类型
     */
    private Integer assetsTypeSubId;

    /**
     * 轮训引擎
     */
    private String pollingEngine;

    /**
     * 监控方式
     */
    private Integer monitorMode;

    /**
     * 厂商
     */
    private String manufacturer;

    /**
     * 规格型号
     */
    private String specifications;

    //品牌
    private String brand;

    /**
     * 描述
     */
    private String description;

    /**
     * 资产状态
     */
    private String enable;

    /**
     * 删除标识符
     */
    private Boolean deleteFlag;

    /**
     * 启动监控状态
     */
    private Boolean monitorFlag;

    /**
     * 启动配置状态
     */
    private Boolean settingFlag;

    private String modifier;

    private String creator;

    private Date createDateStart;

    private Date createDateEnd;

    private Date modificationDateStart;

    private Date modificationDateEnd;

    private List<QueryTangAssetsLabelParam> labelList;

    private List<MwAllLabelDTO> allLabelList;

    /**
     * 是否高级查询
     */
    private Boolean isSelectLabel = false;

    private String prem;

    private Integer userId;

    private List<Integer> groupIds;

    private List<Integer> orgIds;

    private String cmds;

    private String accountId;

    /**
     * 多个资产ID集合
     */
    private List<String> assetsIdList;

    /**
     * 模糊查询字段
     */
    private String fuzzyQuery;
}
