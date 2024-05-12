package cn.mw.monitor.api.dataview;

import cn.mw.monitor.service.scan.model.ScanResultFail;
import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class AssetsResultFailView implements AssetsResultView{
    @ApiModelProperty(value="ID")
    @JSONField(ordinal = 1)
    private int id;

    @ApiModelProperty(value="IP地址")
    @JSONField(ordinal = 2)
    private String ip;

    @ApiModelProperty(value="扫描协议")
    @JSONField(ordinal = 3)
    private String proto;

    @ApiModelProperty(value="描述")
    @JSONField(ordinal = 4)
    private String failCause;

    public void init(ScanResultFail value){
        this.ip = value.getIpAddress();
        this.proto = value.getMonitorMode();
        this.failCause = value.getCause();
    }
}
