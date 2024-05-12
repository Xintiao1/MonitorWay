package cn.mw.monitor.service.scan.param;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class QueryScanResultParam extends BaseParam {
    private String id;
    private Integer scanruleId;
    private String scanBatch;
    private String hostName;
    private String ipAddress;
    private String brand;
    private String description;
    private String specifications;
    private String resulttype;
    private String cause;
    private String monitorMode;
    private boolean isReScanResult;
    @ApiModelProperty(value="资产是否添加")
    private String isExist;

    private String fuzzyQuery;
    private String value;

    //是否模型管理调用此方法
    private Boolean isNewVersion;

    //资产视图纳管使用
    private List<Integer> instanceIds;

    //模型管理纳管资产使用
    private List<Integer> afreshScanRuleIds;

    private Boolean batchManage;
}
