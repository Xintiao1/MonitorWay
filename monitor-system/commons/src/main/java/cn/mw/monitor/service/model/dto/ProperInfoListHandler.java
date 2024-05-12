package cn.mw.monitor.service.model.dto;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ProperInfoListHandler extends BaseTypeHandler<List<PropertyInfo>> {
    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, List<PropertyInfo> propertyInfos, JdbcType jdbcType) throws SQLException {
        String json = JSON.toJSONString(propertyInfos);
        preparedStatement.setString(i, json);
    }

    @Override
    public List<PropertyInfo> getNullableResult(ResultSet resultSet, String columnName) throws SQLException {
        String json = resultSet.getString(columnName);
        List<PropertyInfo> propertyInfos = JSONArray.parseArray(json ,PropertyInfo.class);
        return propertyInfos;
    }

    @Override
    public List<PropertyInfo> getNullableResult(ResultSet resultSet, int i) throws SQLException {
        String json = resultSet.getString(i);
        List<PropertyInfo> propertyInfos = JSONArray.parseArray(json ,PropertyInfo.class);
        return propertyInfos;
    }

    @Override
    public List<PropertyInfo> getNullableResult(CallableStatement callableStatement, int columnIndex) throws SQLException {
        String json = callableStatement.getString(columnIndex);
        List<PropertyInfo> propertyInfos = JSONArray.parseArray(json ,PropertyInfo.class);
        return propertyInfos;
    }
}
