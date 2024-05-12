package cn.mw.syslog.dao;

import cn.mw.monitor.service.systemLog.dto.LoginLogDTO;
import cn.mw.monitor.service.systemLog.dto.SysLogDTO;

import cn.mw.monitor.service.systemLog.param.SystemLogParam;
import cn.mw.syslog.model.MwSysLogEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface MwSysLogDao {

    int insert(@Param("mwSysLogEntity")MwSysLogEntity mwSysLogEntity, @Param("tableName")String tableName,@Param("LOGTBSNAME") String datasource);

    int saveLoginLog(@Param("loginLogDTO") LoginLogDTO loginLogDTO, @Param("tableName")String tableName,@Param("LOGTBSNAME") String datasource);

    List<MwSysLogEntity> selectList(Map record, @Param("tableName")String tableName,@Param("LOGTBSNAME") String datasource);

    List<String> selectTableName(@Param("tableNameType")String tableNameType,@Param("LOGTBSNAME") String datasource);

    List<SysLogDTO> selectSysLog(SystemLogParam systemLogParam, @Param("tableName") String tableName,@Param("LOGTBSNAME") String datasource);

    List<LoginLogDTO> selectLoginLog(@Param("systemLogParam") SystemLogParam systemLogParam,@Param("tableName") String tableName,@Param("LOGTBSNAME") String datasource);

}

