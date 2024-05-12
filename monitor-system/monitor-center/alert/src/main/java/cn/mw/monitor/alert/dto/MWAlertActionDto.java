package cn.mw.monitor.alert.dto;

import cn.mw.monitor.common.bean.BaseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * @author xhy
 * @date 2020/4/1 10:54
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class MWAlertActionDto extends BaseDTO {
    private Integer actionId;
    private String actionName;
    private String discribe;
    private String type;
    private String rule;
    private Integer priority;
    private List<String> username;
    private List<String> noticeType;

}
