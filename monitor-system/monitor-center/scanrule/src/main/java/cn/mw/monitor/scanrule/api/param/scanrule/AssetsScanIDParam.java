package cn.mw.monitor.scanrule.api.param.scanrule;

import lombok.Data;

@Data
public class AssetsScanIDParam {
    private Integer scanRuleId;
    //扫描结果类型
    private String resulttype;
    //是否新版本（新版本指模型管理下的资产发现；）
    private Boolean isNewVersion;
}
