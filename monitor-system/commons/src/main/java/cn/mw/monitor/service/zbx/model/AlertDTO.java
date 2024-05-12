package cn.mw.monitor.service.zbx.model;

import cn.mw.monitor.service.alert.dto.ZbxAlertDto;
import lombok.Data;

@Data
public class AlertDTO {
    private String ip;
    private String acknowledged;//确认状态
    private String message;

    public void extractFrom(ZbxAlertDto zbxAlertDto){
        this.acknowledged = zbxAlertDto.getAcknowledged();
        this.message = zbxAlertDto.getMessage();
        this.ip = zbxAlertDto.getIp();
    }
}
