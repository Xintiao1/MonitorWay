package cn.mw.monitor.screen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @ClassName MWNewScreenAlertDevOpsEventDto
 * @Description ToDo
 * @Author gengjb
 * @Date 2021/11/30 10:08
 * @Version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MWNewScreenAlertDevOpsEventDto {

    //总告警条数
    private int tolatAlertCount;

    //未确认告警条数
    private List<Map<String,Object>> alertCount;

//    //已确认告警条数
//    private Map<String,Integer> insertCount;

    private Integer userId;

    private Integer modelId;

    private String modelDataId;

    private MessageCount todayMessage;
    private MessageCount sumMessage;

    private String name;

    private Integer mwRankCount;

    private Integer dateType;

    private String startTime;

    private String endTime;

    private Map<String,List<ActivityAlertClassifyDto>> alertClassiftMap;

    //是否缓存
    private Boolean isCache;

    //监控项名称
    private List<String> itemNames;
}
