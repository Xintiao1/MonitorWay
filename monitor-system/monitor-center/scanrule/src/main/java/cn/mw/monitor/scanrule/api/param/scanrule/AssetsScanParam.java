package cn.mw.monitor.scanrule.api.param.scanrule;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;


@ApiModel(value = "资产扫描")
@Data
public class AssetsScanParam {
    //引擎id
    private String engineId;
    private String name;//规则名称
    private String updateinterval;
    private boolean executeNow = false;
    private List<IPRangeParam> ipRange;//ip范围
    private List<IPSubnetParam> ipsubnets;//ip地址段
    private String iplist;
    private boolean ipv6checked;
    private List<AssetsScanRuleParam> scanrules;
    private Integer ruleId;

    //单个添加资产页面添加资产时,前端需要设置该参数,逻辑删除,保证该规则不可见
    private Integer isdelete;

    //服务器id
    private int monitorServerId;

    //重新扫描时的id列表
    private List<Integer> rescanIds;

    //扫描结果类型
    private String resulttype;
    private boolean nmapFlag;
    //是否新版本（新版本指模型管理下的资产发现；）
    private Boolean isNewVersion;

    //是否模型实例下的资产扫描，默认立即执行
    private Boolean isExecute;
}
