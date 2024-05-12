package cn.mw.monitor.model.dto;

import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ModelInstanceTypeHandler extends BaseTypeHandler<ModelInstanceTopoInfo> {
    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, ModelInstanceTopoInfo modelInstanceTopoInfo, JdbcType jdbcType) throws SQLException {
        String json = JSON.toJSONString(modelInstanceTopoInfo);
        preparedStatement.setString(i, json);
    }

    @Override
    public ModelInstanceTopoInfo getNullableResult(ResultSet resultSet, String s) throws SQLException {
        String json = resultSet.getString(s);
        if(StringUtils.isNotEmpty(json)){
            ModelInstanceTopoInfo modelInstanceTopoInfo = JSON.parseObject(json ,ModelInstanceTopoInfo.class);
            return modelInstanceTopoInfo;
        }
        return null;
    }

    @Override
    public ModelInstanceTopoInfo getNullableResult(ResultSet resultSet, int i) throws SQLException {
        String json = resultSet.getString(i);
        if(StringUtils.isNotEmpty(json)){
            ModelInstanceTopoInfo modelInstanceTopoInfo = JSON.parseObject(json ,ModelInstanceTopoInfo.class);
            return modelInstanceTopoInfo;
        }
        return null;
    }

    @Override
    public ModelInstanceTopoInfo getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        String json = callableStatement.getString(i);
        if(StringUtils.isNotEmpty(json)){
            ModelInstanceTopoInfo modelInstanceTopoInfo = JSON.parseObject(json ,ModelInstanceTopoInfo.class);
            return modelInstanceTopoInfo;
        }
        return null;
    }
}
