package cn.mw.monitor.screen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @ClassName MWTangibleassetsDto
 * @Description 资产信息数据
 * @Author gengjb
 * @Date 2022/1/21 11:15
 * @Version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MWTangibleassetsDto {

    private String id;

    private String assetsId;

    private String assetsName;

    private Integer monitorServerId;

    private String typeName;

    private List<String> orgNames;

    //是否关键设备
    private Boolean isKeyDevices;
}
