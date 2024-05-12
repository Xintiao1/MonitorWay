package cn.huaxing.utils;

import cn.huaxing.dto.HuaxingVisualizedDataDto;
import cn.huaxing.dto.HuaxingVisualizedDataSourceDto;
import cn.huaxing.dto.HuaxingVisualizedDataSourceSqlDto;
import cn.mwpaas.common.utils.CollectionUtils;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author gengjb
 * @description 华星数据库连接工具类
 * @date 2023/8/28 11:35
 */
@Slf4j
public class HuaxingDataBaseConnectionUtil {

    /**
     * 连接数据库并查询结果返回
     */
    public static List<HuaxingVisualizedDataDto> connectionDataBase(List<HuaxingVisualizedDataSourceDto> dataSourceDtos) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        List<HuaxingVisualizedDataDto> realDatas = new ArrayList<>();
        try {
            for (HuaxingVisualizedDataSourceDto dataSourceDto : dataSourceDtos) {
                log.info("HuaxingDataBaseConnectionUtil{} connectionDataBase() dataSourceDto::"+dataSourceDto);
                if (CollectionUtils.isEmpty(dataSourceDto.getDataQuerySqls())) {
                    continue;
                }
                Class.forName(dataSourceDto.getDriver());
                try {
                    connection = DriverManager.getConnection(dataSourceDto.getUrl(), dataSourceDto.getUserName(), dataSourceDto.getPassWord());
                }catch (Throwable e){
                    connection.close();
                    log.error("HuaxingDataBaseConnectionUtil{} connectionDataBase() connectionError",e);
                    continue;
                }
                List<HuaxingVisualizedDataSourceSqlDto> dataQuerySqls = dataSourceDto.getDataQuerySqls();
                for (HuaxingVisualizedDataSourceSqlDto dataQuerySql : dataQuerySqls) {
                    List<Map<String,Object>> resultList = new ArrayList<>();
                    String sqlString = dataQuerySql.getSqlString();
                    log.info("HuaxingDataBaseConnectionUtil{} connectionDataBase() sqlString::"+sqlString+":::chartType"+dataQuerySql.getCharType()+"::::partitionName::"+dataQuerySql.getPartitionName());
                    statement = connection.prepareStatement(sqlString);
                    rs = statement.executeQuery();
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        for (int i = 1; i <= columnCount; i++) {
                            String columnName = metaData.getColumnLabel(i); // 获取列名
                            Object value = rs.getObject(i); // 获取列的值
                            row.put(columnName, value);
                        }
                        resultList.add(row);
                    }
                    log.info("HuaxingDataBaseConnectionUtil{} connectionDataBase() resultList::"+resultList+":::chartType"+dataQuerySql.getCharType()+"::::partitionName::"+dataQuerySql.getPartitionName());
                    HuaxingVisualizedDataDto visualizedDataDto = new HuaxingVisualizedDataDto();
                    visualizedDataDto.setChartType(dataQuerySql.getCharType());
                    visualizedDataDto.setPartitionName(dataQuerySql.getPartitionName());
                    visualizedDataDto.setDataList(resultList);
                    realDatas.add(visualizedDataDto);
                }
            }
        } catch (Throwable e) {
            log.error("HuaxingDataBaseConnectionUtil{} connectionDataBase() error", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Throwable e) {
                    log.error("HuaxingDataBaseConnectionUtil{} connectionDataBase() connectionCloseError", e);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (Throwable e) {
                    log.error("HuaxingDataBaseConnectionUtil{} connectionDataBase() statementCloseError", e);
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (Throwable e) {
                    log.error("HuaxingDataBaseConnectionUtil{} connectionDataBase() rsCloseError", e);
                }
            }
        }
        return realDatas;
    }
}
