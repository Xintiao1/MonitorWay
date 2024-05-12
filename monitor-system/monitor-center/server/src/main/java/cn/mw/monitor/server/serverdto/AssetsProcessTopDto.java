package cn.mw.monitor.server.serverdto;

import cn.mw.monitor.service.alert.dto.ZbxAlertDto;
import cn.mw.monitor.service.server.api.dto.ItemApplication;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author gengjb
 * @description TODO
 * @date 2023/7/14 11:22
 */
@Data
@ApiModel("资产详情进程DTO")
public class AssetsProcessTopDto {

    private String alertDate;

    private String alertTitle;

    private String value;

    public void extractFrom(ZbxAlertDto zbxAlertDto,ItemApplication itemApplication){
        this.alertDate = zbxAlertDto.getClock();
        this.alertTitle = zbxAlertDto.getName();
        this.value = itemApplication.getLastvalue();
    }
}
