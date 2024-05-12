package cn.mw.monitor.service.server.api.dto;

import com.github.pagehelper.PageInfo;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author syt
 * @Date 2021/6/21 11:26
 * @Version 1.0
 */
@Data
public class ApplicationTableInfos {
    //表格数据
    private Map<String, List<Map<String, Object>>> allData;

    //表头数据
    private Map<String, List<ColTable>> titleData;
    //分页后数据
    private Map<String, PageInfo> pageInfo;
}
