package cn.mw.monitor.visualized.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @ClassName
 * @Description Prometheus值数据DTO
 * @Author gengjb
 * @Date 2023/6/7 14:13
 * @Version 1.0
 **/
@Data
@ApiModel("Prometheus值数据DTO")
public class MwPromValueDto {

    //时间，秒
    private String clock;

    //值
    private String value;
}
