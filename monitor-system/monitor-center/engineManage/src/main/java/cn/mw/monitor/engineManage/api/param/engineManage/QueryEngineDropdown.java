package cn.mw.monitor.engineManage.api.param.engineManage;

import lombok.Data;

import java.util.List;

/**
 * @author syt
 * @Date 2021/9/6 16:48
 * @Version 1.0
 */
@Data
public class QueryEngineDropdown {

    private List<Integer> monitorServerIds;
}
