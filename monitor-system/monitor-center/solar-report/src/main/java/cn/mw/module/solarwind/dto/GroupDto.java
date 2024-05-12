package cn.mw.module.solarwind.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author xhy
 * @date 2020/7/3 14:14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupDto {
    private Integer tag;
    private String carrierName;
    private Integer interfaceID;
    private String periodRadio;
    private String inColumn;
    private String outColumn;
    private Float percentFront;
    private Float percentBack;
    private String tableName;
    private String startTime;
    private String endTime;
    private String startTimeDay;
    private String endTimeDay;
    private List<Integer> interfaceIDs;
}
