package cn.mw.monitor.interceptor;

import com.github.pagehelper.Page;
import com.github.pagehelper.dialect.AbstractHelperDialect;
import com.github.pagehelper.util.MetaObjectUtil;
import com.github.pagehelper.util.StringUtil;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MysqlPageDialectImp extends AbstractHelperDialect {
    private static final Logger logger = LoggerFactory.getLogger("component-" + MysqlPageDialectImp.class.getName());

    @Override
    public String getCountSql(MappedStatement ms, BoundSql boundSql, Object parameterObject, RowBounds rowBounds, CacheKey countKey) {
        Page<Object> page = this.getLocalPage();
        String countColumn = page.getCountColumn();
        String sql = null;
        if(StringUtil.isNotEmpty(countColumn)){
            sql =  this.countSqlParser.getSmartCountSql(boundSql.getSql(), countColumn);
        }else{
            sql = this.countSqlParser.getSmartCountSql(boundSql.getSql());
        }
        DataPermissionSql dataPermission = DataPermUtil.getDataPerm();
        if(null != dataPermission){
            sql = dataPermission.changCountsql(sql);
        }

        return  sql;
    }

    @Override
    public Object processPageParameter(MappedStatement ms, Map<String, Object> paramMap, Page page, BoundSql boundSql, CacheKey pageKey) {
        paramMap.put("First_PageHelper", page.getStartRow());
        paramMap.put("Second_PageHelper", page.getPageSize());
        pageKey.update(page.getStartRow());
        pageKey.update(page.getPageSize());
        if (boundSql.getParameterMappings() != null) {
            List<ParameterMapping> newParameterMappings = new ArrayList(boundSql.getParameterMappings());
            if (page.getStartRow() == 0) {
                newParameterMappings.add((new ParameterMapping.Builder(ms.getConfiguration(), "Second_PageHelper", Integer.class)).build());
            } else {
                newParameterMappings.add((new ParameterMapping.Builder(ms.getConfiguration(), "First_PageHelper", Integer.class)).build());
                newParameterMappings.add((new ParameterMapping.Builder(ms.getConfiguration(), "Second_PageHelper", Integer.class)).build());
            }

            MetaObject metaObject = MetaObjectUtil.forObject(boundSql);
            metaObject.setValue("parameterMappings", newParameterMappings);
        }

        return paramMap;
    }

    @Override
    public String getPageSql(String sql, Page page, CacheKey cacheKey) {

        String retsql = sql;
        DataPermissionSql dataPermission = DataPermUtil.getDataPerm();
        if(null != dataPermission){
            try {
                retsql = dataPermission.changeSql(sql);
            }catch (Throwable t) {
                logger.error(t.getMessage());
            }finally {
                DataPermUtil.remove();
            }
        }

        StringBuilder sqlBuilder = new StringBuilder(retsql.length() + 14);
        sqlBuilder.append(retsql);
        if (page.getStartRow() == 0) {
            sqlBuilder.append(" LIMIT ? ");
        } else {
            sqlBuilder.append(" LIMIT ?, ? ");
        }

        return sqlBuilder.toString();
    }
}
