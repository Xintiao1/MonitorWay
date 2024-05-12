package cn.mw.config.syslog;


import ch.qos.logback.classic.spi.CallerData;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.db.ConnectionSource;
import ch.qos.logback.core.db.DBAppenderBase;
import ch.qos.logback.core.db.DriverManagerConnectionSource;
import cn.mw.monitor.api.common.SpringUtils;
import cn.mw.monitor.common.bean.SystemLogDTO;
import cn.mw.monitor.common.constant.Constants;
import cn.mw.monitor.util.getLogTableNameUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MySDBAppender extends DBAppenderBase<ILoggingEvent> {

    private boolean initialized = false;
    private String insertSQL;
    private static final Method GET_GENERATED_KEYS_METHOD;

    private static final String TABLE_NAME_TYPE = "mw_system_log";
    private static  String MSQURL = "";

    private static final int TIME_INDEX = 1;
    private static final int USER_INDEX = 1;
    private static final int MODEL_INDEX = 2;
    private static final int OBJ_INDEX = 3;
    private static final int OPERATE_INDEX = 4;
    private static final int TYPE_INDEX = 5;
    private static final Pattern pattern = Pattern.compile(".*Table.*((mw_system_log)|(mw_login_log)|(mw_sys_log))_.*doesn\'t exist.*");

    private static final StackTraceElement EMPTY_CALLER_DATA = CallerData.naInstance();

    static {
        // PreparedStatement.getGeneratedKeys() method was added in JDK 1.4
        Method getGeneratedKeysMethod;
        try {
            // the
            getGeneratedKeysMethod = PreparedStatement.class.getMethod("getGeneratedKeys", (Class[]) null);
        } catch (Exception ex) {
            getGeneratedKeysMethod = null;
        }
        GET_GENERATED_KEYS_METHOD = getGeneratedKeysMethod;
    }

    @Override
    public void start() {
        ConnectionSource connectionSource = getConnectionSource();

        if(null != connectionSource
        && connectionSource instanceof DriverManagerConnectionSource){
            DriverManagerConnectionSource driverManagerConnectionSource = (DriverManagerConnectionSource) connectionSource;
            if(StringUtils.isNotEmpty(driverManagerConnectionSource.getUrl())){
                MSQURL= driverManagerConnectionSource.getUrl();
                insertSQL = buildInsertSQL(driverManagerConnectionSource.getUrl());
                initialized = true;
                super.start();
            }
        }
    }

    @Override
    public void append(ILoggingEvent eventObject) {
        if(initialized) {
            super.append(eventObject);
        }

    }

    private static String buildInsertSQL(String url) {
        String tableName = getLogTableNameUtils.getTableNameByType(Calendar.getInstance(), TABLE_NAME_TYPE);
        String sql = "";
      if (url.contains(Constants.DATABASE_MYSQL)){
          sql = "INSERT INTO "+ tableName  +
                  "(log_time, user_name,model_name,obj_name,operate_des,type)" +
                  "VALUES (now(), ?, ? ,?, ?, ?)";
      }else {
          sql = "INSERT INTO \""+tableName+"\"  (\"id\", \"log_time\",\"user_name\") " +
                  "VALUES (1111,sysdate,?)";
      }
        return sql;
    }

    private void bindLoggingEventWithInsertStatement(PreparedStatement stmt, ILoggingEvent event) throws SQLException {
       /* stmt.setTimestamp(TIME_INDEX, new Timestamp(event.getTimeStamp()));*/
        SystemLogDTO systemLogDTO = JSON.parseObject(event.getMessage(), SystemLogDTO.class);
        stmt.setString(USER_INDEX, systemLogDTO.getUserName());
        stmt.setString(MODEL_INDEX, systemLogDTO.getModelName());
        stmt.setString(OBJ_INDEX, systemLogDTO.getObjName());
        stmt.setString(OPERATE_INDEX, systemLogDTO.getOperateDes());
        stmt.setString(TYPE_INDEX, systemLogDTO.getType());
    }



    @Override
    protected void subAppend(ILoggingEvent event, Connection connection, PreparedStatement insertStatement) throws Throwable {
        // This is expensive... should we do it every time?
        try {
            if (MSQURL.contains(Constants.DATABASE_MYSQL)){
                bindLoggingEventWithInsertStatement(insertStatement, event);
                int updateCount = insertStatement.executeUpdate();
                if (updateCount != 1) {
                    addWarn("Failed to insert loggingEvent");
                }
            }else {
                    String oracle = getOracle(event);
                    insertStatement.execute(oracle);
            }
        }catch (Exception e){
            String errorMsg = e.getMessage();
            Matcher m = pattern.matcher(errorMsg);
            if (m.find( )) {
                try {
                    Object mwCreateLogTable = SpringUtils.getBean("mwCreateLogTable");
                    if(null != mwCreateLogTable){
                        Method method = mwCreateLogTable.getClass().getMethod("init");
                        if(null != method){
                            method.invoke(mwCreateLogTable);
                        }
                    }
                    return;
                }catch (Exception e1){
                    throw e1;
                }
            }
            throw e;
        }
    }

    private String getOracle(ILoggingEvent event) {
        String tableName = getLogTableNameUtils.getTableNameByType(Calendar.getInstance(), TABLE_NAME_TYPE);
        SystemLogDTO systemLogDTO = JSON.parseObject(event.getMessage(), SystemLogDTO.class);
      /*  stmt.setString(USER_INDEX, systemLogDTO.getUserName());
        stmt.setString(MODEL_INDEX, systemLogDTO.getModelName());
        stmt.setString(OBJ_INDEX, systemLogDTO.getObjName());
        stmt.setString(OPERATE_INDEX, systemLogDTO.getOperateDes());
        stmt.setString(TYPE_INDEX, systemLogDTO.getType());*/
        String sql = "INSERT INTO \""+tableName+"\"  (\"id\", \"log_time\",\"user_name\", \"model_name\", \"type\", \"obj_name\", \"operate_des\") " +
                "VALUES (SEQ_LOG_INDEX.NEXTVAL,sysdate,'"+systemLogDTO.getUserName()+"','"+systemLogDTO.getModelName()+"','"+systemLogDTO.getType()+"',TO_NCHAR('"+systemLogDTO.getObjName()+"'),TO_NCHAR('"+systemLogDTO.getOperateDes()+"'))";
        return sql;
    }

    private StackTraceElement extractFirstCaller(StackTraceElement[] callerDataArray) {
        StackTraceElement caller = EMPTY_CALLER_DATA;
        if (hasAtLeastOneNonNullElement(callerDataArray))
            caller = callerDataArray[0];
        return caller;
    }

    private boolean hasAtLeastOneNonNullElement(StackTraceElement[] callerDataArray) {
        return callerDataArray != null && callerDataArray.length > 0 && callerDataArray[0] != null;
    }

    @Override
    protected Method getGeneratedKeysMethod() {
        return GET_GENERATED_KEYS_METHOD;
    }

    @Override
    protected String getInsertSQL() {
        return insertSQL;
    }

    protected void secondarySubAppend(ILoggingEvent event, Connection connection, long eventId) throws Throwable {
    }
}
