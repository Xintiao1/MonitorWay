package cn.mw.monitor.logManage.service;

import cn.mw.monitor.logManage.param.LogAnalysisParam;
import cn.mw.monitor.logManage.vo.TableNameInfo;

import java.util.List;
import java.util.Map;

public interface MwLogManageService {

    List<TableNameInfo> getTables();

    List<Map<String, Object>> getColumnByTable(String tableName);

    Object list(LogAnalysisParam logAnalysisParam);

    Object logAnalysisChar(LogAnalysisParam logAnalysisParam);

    String saveSelectedColumns(String columnsParam);

    Object getLogAnalysisCacheInfo();
}
