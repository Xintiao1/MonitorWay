package cn.mw.monitor.accountmanage.dao;


import cn.mw.monitor.accountmanage.entity.AlertRecordTableDTO;
import cn.mw.monitor.accountmanage.entity.MwAlerthistory7daysParam;

import java.util.List;

public interface MwAlerthistory7daysTableDao {

  Integer insert(List<MwAlerthistory7daysParam> mwAlerthistory7daysParam);

  List<MwAlerthistory7daysParam> getAlertHistory(Integer userId);

  Integer truncateTable();

}
