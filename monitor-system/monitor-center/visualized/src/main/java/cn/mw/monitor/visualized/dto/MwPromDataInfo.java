package cn.mw.monitor.visualized.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

/**
 * @ClassName
 * @Description Prometheus指标属性和值DTO
 * @Author gengjb
 * @Date 2023/6/7 14:00
 * @Version 1.0
 **/
@Data
@ApiModel("可视化缓存DTO")
public class MwPromDataInfo {

    private String resultType;

    private List<MwPromMetric> result;
}
