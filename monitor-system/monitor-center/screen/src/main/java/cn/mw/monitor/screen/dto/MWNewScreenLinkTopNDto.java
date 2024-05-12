package cn.mw.monitor.screen.dto;

import cn.mw.monitor.service.model.dto.DetailPageJumpDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName MWNewScreenLinkTopNDto
 * @Description ToDo
 * @Author gengjb
 * @Date 2021/12/1 10:09
 * @Version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MWNewScreenLinkTopNDto extends DetailPageJumpDto {

    private String name;
    private String assetsId;

    private String id;

    private String ip;

    private Double sendLastValue;
    private Double sortlastValue;
    private String type;
    private String sendUnits;
    private String value;
    private Integer monitorServerId;

    private Boolean isWebMonitor;

    private String assetsStatus;

    private Double acceptLastValue;

    private String acceptUnits;

    private Double secondSendValue;

    private String secondSendUnits;

    private Double secondAcceptValue;

    private String secondAcceptUnits;

    private Double totalSort;

}
