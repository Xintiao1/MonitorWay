package cn.mw.monitor.TPServer.dto;

import lombok.Data;

import java.util.List;

/**
 * @author syt
 * @Date 2020/10/30 17:29
 * @Version 1.0
 */
@Data
public class DeleteTPServerParam {
    private List<Integer> ids;
}
