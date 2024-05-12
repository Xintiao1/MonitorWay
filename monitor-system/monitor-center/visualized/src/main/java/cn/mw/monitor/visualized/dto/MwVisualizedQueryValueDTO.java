package cn.mw.monitor.visualized.dto;

import lombok.Data;

/**
 * @ClassName MwVisualizedQueryValueDTO
 * @Author gengjb
 * @Date 2022/4/21 15:34
 * @Version 1.0
 **/
@Data
public class MwVisualizedQueryValueDTO {
    private Integer id;
    private String queryValueJsonStr;
    private MwVisualizedViewDto queryValue;
}
