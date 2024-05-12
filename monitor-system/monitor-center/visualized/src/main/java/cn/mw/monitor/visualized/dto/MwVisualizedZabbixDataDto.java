package cn.mw.monitor.visualized.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @ClassName MwVisualizedZabbixDataDto
 * @Description ToDo
 * @Author gengjb
 * @Date 2022/4/26 15:16
 * @Version 1.0
 **/
@Data
@Builder
public class MwVisualizedZabbixDataDto {

    private String hostId;

    private String itemId;

    private String assetsId;

    private String assetsName;

    private String ip;

    private String name;

    private String units;

    private Integer valueType;

    private Double maxValue;

    private Double minValue;

    private String fieldName;

    private Double avgValue;

    private String interfaceName;


    List<MwVisualizedZabbixHistoryDto> values;

    private String originUtits;

    private int isExport;
}
