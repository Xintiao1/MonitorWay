package cn.mw.monitor.report.dto;

/**
 * @author xhy
 * @date 2020/5/11 12:15
 */

import lombok.Data;

import java.util.List;

@Data
public class DeleteParam {
    private List<String> reportIdList;
}
