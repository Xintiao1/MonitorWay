package cn.mw.syslog.service.impl;

import cn.mw.monitor.common.bean.SystemLogDTO;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.service.common.ServiceException;
import cn.mw.monitor.service.systemLog.api.MwSysLogService;
import cn.mw.monitor.service.systemLog.dto.LogTypeEnum;
import cn.mw.monitor.service.systemLog.dto.LoginLogDTO;
import cn.mw.monitor.service.systemLog.dto.SysLogDTO;
import cn.mw.monitor.service.systemLog.param.EditLogParam;
import cn.mw.monitor.service.systemLog.param.SystemLogParam;
import cn.mw.monitor.service.systemLog.param.UpdateAttribute;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.syslog.dao.MwSysLogDao;
import cn.mw.syslog.utils.GenerationTableName;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.List;

@Slf4j
@Service
@Transactional
public class MwSysLogServiceImpl implements MwSysLogService {

    private static final Logger dbLogger = LoggerFactory.getLogger("MWDBLogger");
    @Resource
    private MwSysLogDao mwSysLogDao;

    @Resource
    private ILoginCacheInfo iLoginCacheInfo;

    @Value("${datasource.log.dataname}")
    private String datasource;
    @Value("${datasource.check}")
    private String CHECK;
    public static final String DATEBASEORACLE = "oracle";

    @Override
    public Reply selectTableName(Integer tableNameType) {
        try {
            String tableType = null;
            if (LogTypeEnum.LOGINTABLENAME.getCode() == tableNameType) {
                tableType = LogTypeEnum.LOGINTABLENAME.getName();
            } else if (LogTypeEnum.SYSTABLENAME.getCode() == tableNameType) {
                tableType = LogTypeEnum.SYSTABLENAME.getName();
            }
            List<String> list = mwSysLogDao.selectTableName(tableType,"'"+datasource+"'");
            List<String> keyList = GenerationTableName.getTableNameKey(list, tableType);
            return Reply.ok(keyList);
        } catch (Exception e) {
            log.error("fail to selectTableName with  cause:【{}】", e.getMessage());
            throw new ServiceException(
                    Reply.fail(ErrorConstant.USERGROUPCODE_250102, ErrorConstant.USERGROUP_MSG_250102));
        }
    }

    @Async
    @Override
    public void saveLoginLog(LoginLogDTO loginLogDTO) {
        try {
            String tableName = GenerationTableName.getTableNameByType(Calendar.getInstance(), LogTypeEnum.LOGINTABLENAME.getName());
            if (CHECK.equals(DATEBASEORACLE)){
                tableName = "\""+tableName+"\"";
            }
            mwSysLogDao.saveLoginLog(loginLogDTO, tableName,datasource);
        } catch (Exception e) {
            log.error("fail to saveLog with UserLogDTO=【{}】, cause:【{}】",
                    loginLogDTO, e.getMessage());
        }
    }

    @Async
    public Reply saveEditLog(EditLogParam param) {
        try {
            StringBuffer sb = new StringBuffer("【变更】");
            List<UpdateAttribute> updateData = param.getUpdateData();
            for (UpdateAttribute datum : updateData) {
                sb.append("[").append(datum.getName() + "]由<").append(datum.getOld() + ">改为<").
                        append(datum.getNow() + ">;");

            }
            SystemLogDTO build = SystemLogDTO.builder().objName(param.getObjName()).
                    modelName(param.getModelName()).userName(iLoginCacheInfo.getLoginName()).
                    operateDes(sb.toString()).build();
            dbLogger.info(JSON.toJSONString(build));
            return Reply.ok();
        } catch (Exception e) {
            return Reply.fail(e.getMessage(), param);
        }
    }

    @Override
    public Reply selectSysLog(SystemLogParam qParam) {
        try {
            if (qParam.getLogType().equals(LogTypeEnum.SYSLOG.getCode())) {
                String tableType = LogTypeEnum.SYSTABLENAME.getName();
                String tableName = null;
                if (StringUtils.isNotEmpty(qParam.getTableNameKey())) {
                    List<String> list = mwSysLogDao.selectTableName(tableType,"'"+datasource+"'");
                    tableName = GenerationTableName.getTablename(list, qParam.getTableNameKey(), tableType);
                } else {
                    tableName = GenerationTableName.getTableNameByType(Calendar.getInstance(), tableType);
                }
                PageHelper.startPage(qParam.getPageNumber(), qParam.getPageSize());
                if (CHECK.equals(DATEBASEORACLE)){
                    tableName = "\""+tableName+"\"";
                }
                List<SysLogDTO> logDTOS = mwSysLogDao.selectSysLog(qParam, tableName,datasource);
                PageInfo pageInfo = new PageInfo(logDTOS);
                pageInfo.setList(logDTOS);
                return Reply.ok(pageInfo);
            } else if (qParam.getLogType().equals(LogTypeEnum.LOGINLOG.getCode())) {
                String tableType = LogTypeEnum.LOGINTABLENAME.getName();
                String tableName = null;
                if (StringUtils.isNotEmpty(qParam.getTableNameKey())) {
                    List<String> list = mwSysLogDao.selectTableName(tableType, "'"+datasource+"'");
                    tableName = GenerationTableName.getTablename(list, qParam.getTableNameKey(), tableType);
                } else {
                    tableName = GenerationTableName.getTableNameByType(Calendar.getInstance(), tableType);
                }
                if (CHECK.equals(DATEBASEORACLE)){
                    tableName = "\""+tableName+"\"";
                }
                //登录日志
                PageHelper.startPage(qParam.getPageNumber(), qParam.getPageSize());
                List<LoginLogDTO> loginLogDTOS = mwSysLogDao.selectLoginLog(qParam, tableName,datasource);
                PageInfo pageInfo = new PageInfo(loginLogDTOS);
                pageInfo.setList(loginLogDTOS);
                return Reply.ok(pageInfo);
            } else {
                return Reply.fail("无此类型日志");
            }
        } catch (Exception e) {
            return Reply.fail(e.getMessage());
        }
    }

    @Override
    public Reply selectSysLogByModel(SystemLogParam qParam) {
        try {
            String tableType = LogTypeEnum.SYSTABLENAME.getName();
            String tableName = null;
            if (StringUtils.isNotEmpty(qParam.getTableNameKey())) {
                List<String> list = mwSysLogDao.selectTableName(tableType,"'"+datasource+"'");
                tableName = GenerationTableName.getTablename(list, qParam.getTableNameKey(), tableType);
            } else {
                tableName = GenerationTableName.getTableNameByType(Calendar.getInstance(), tableType);
            }
            if (CHECK.equals(DATEBASEORACLE)){
                tableName = "\""+tableName+"\"";
            }
            List<SysLogDTO> logDTOS = mwSysLogDao.selectSysLog(qParam, tableName,datasource);
            return Reply.ok(logDTOS);
        } catch (Exception e) {
            return Reply.fail(e.getMessage());
        }
    }

}
