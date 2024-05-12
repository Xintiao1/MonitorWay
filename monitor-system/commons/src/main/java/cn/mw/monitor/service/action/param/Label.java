package cn.mw.monitor.service.action.param;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author xhy
 * @date 2020/9/21 15:41
 */
@Data
public class Label {
    private String actionId;
    private String labelName;
    private Integer labelId;
    private Integer inputFormat;
    private String labelValue;
    private Integer dropKey;
    private String dropdownValue;
    private Date labelDateStart;
    private Date labelDateEnd;
    private List<Date> labelTimeValue;
}
