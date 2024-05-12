package cn.mw.monitor.visualized.dto;

import cn.mw.monitor.service.server.api.dto.ItemApplication;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

/**
 * @ClassName
 * @Description 可视化告警DTO
 * @Author gengjb
 * @Date 2023/5/19 23:06
 * @Version 1.0
 **/
@Data
@ApiModel("可视化告警DTO")
public class MwVisualizedAlertDto {

    private String eventid;

    private String severity;

    private List<ItemApplication> hosts;

    private String name;
}
