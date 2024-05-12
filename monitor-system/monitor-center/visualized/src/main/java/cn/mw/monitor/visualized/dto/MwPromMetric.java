package cn.mw.monitor.visualized.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName
 * @Description Prometheus值与标签DTO
 * @Author gengjb
 * @Date 2023/6/7 14:12
 * @Version 1.0
 **/
@Data
@ApiModel("Prometheus值与标签DTO")
public class MwPromMetric {

    /**  * metric name和描述当前样本特征的labelsets  */
    private MwPromMetricInfo metric;

    /**  * 一个float64的浮点型数据表示当前样本的值。  */
    private String[] value;

    /**  * 一个float64的浮点型数据表示当前样本的值。  */
    private String[][] values;

    List<MwPromValueDto> promValue;

    public void setValue(String[] value) {
        this.value = value;
        if(promValue == null){
            promValue = new ArrayList<>();
        }
        MwPromValueDto dto = new MwPromValueDto();
        dto.setClock(value[0]);
        dto.setValue(value[1]);
        promValue.add(dto);
    }

    public void setValues(String[][] values) {
        this.values = values;
        if(promValue == null){
            promValue = new ArrayList<>();
        }
        for (String[] value : values) {
            MwPromValueDto dto = new MwPromValueDto();
        dto.setClock(value[0]);
        dto.setValue(value[1]);
        promValue.add(dto);
        }
    }
}
