package cn.mw.monitor.api.dataview;

import cn.mw.monitor.scanrule.dto.MwScanruleDTO;
import cn.mw.monitor.service.scanrule.model.Perform;
import cn.mw.monitor.util.AssetsUtils;
import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Data
public class AssetsDiscoverRuleView {

    @ApiModelProperty(value="规则ID")
    @JSONField(ordinal = 1)
    private String id;

    @ApiModelProperty(value="规则名称")
    @JSONField(ordinal = 2)
    private String name;

    @JSONField(ordinal = 3)
    private Perform perform;

    @ApiModelProperty(value="开始时间")
    @JSONField(ordinal = 4)
    private String scanStartTime;

    @ApiModelProperty(value="时长")
    @JSONField(ordinal = 5)
    private String timeLength;

    @ApiModelProperty(value="执行人")
    @JSONField(ordinal = 6)
    private String executor;

    @JSONField(ordinal = 7)
    private String operation;

    public void init(MwScanruleDTO mwScanruleDTO){
        this.id = mwScanruleDTO.getScanruleId().toString();
        this.name = mwScanruleDTO.getScanruleName();

        this.perform = mwScanruleDTO.getPerform();

        this.executor = mwScanruleDTO.getModifier();

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date scanStart = mwScanruleDTO.getScanStartDate();
        this.scanStartTime = null != scanStart?dateFormat.format(scanStart):null;
        this.timeLength = AssetsUtils.scanRuleTime(mwScanruleDTO.getScanStartDate(), mwScanruleDTO.getScanEndDate());

        this.operation = "op";
    }
}
