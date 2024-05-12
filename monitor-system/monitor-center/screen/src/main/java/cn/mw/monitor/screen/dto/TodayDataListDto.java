package cn.mw.monitor.screen.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author xhy
 * @date 2020/10/21 13:18
 */
@Data
public class TodayDataListDto {
    private List<Map<String,Object>> list;
    private String units;
}
