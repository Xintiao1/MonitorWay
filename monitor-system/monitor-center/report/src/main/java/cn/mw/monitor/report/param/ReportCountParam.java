package cn.mw.monitor.report.param;

import lombok.Data;

import java.util.List;

/**
 * @author xhy
 * @date 2020/12/9 16:01
 */
@Data
public class ReportCountParam {
    private Integer userId;
    private List<Integer> groupIds;
    private List<Integer> orgIds;
    private Boolean isAdmin;

}
