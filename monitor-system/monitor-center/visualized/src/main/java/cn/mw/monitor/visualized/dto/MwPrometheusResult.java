package cn.mw.monitor.visualized.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @ClassName
 * @Description 调用Prometheus返回结果转换实体类
 * @Author gengjb
 * @Date 2023/6/7 11:17
 * @Version 1.0
 **/
@Data
@ApiModel("可视化缓存DTO")
public class MwPrometheusResult {

    /**
     *  状态
     *  成功-- success
     */
    private String status;
    /**
     * prometheus指标属性和值
     * */
    private MwPromDataInfo data;
}
