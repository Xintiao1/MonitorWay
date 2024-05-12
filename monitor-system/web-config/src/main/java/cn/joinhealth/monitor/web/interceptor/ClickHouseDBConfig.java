package cn.joinhealth.monitor.web.interceptor;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;

@Configuration
@ConditionalOnProperty(prefix = "clickhouse", name = "url")
@MapperScan(basePackages  = ClickHouseDBConfig.PACKAGE , sqlSessionFactoryRef = "mclickhouseSqlSessionFactory")
public class ClickHouseDBConfig {
    public static final String PACKAGE = "cn.mw.clickhouse.dao";
    private static final String MAPPER_LOCATION_CLICKHOUSE = "classpath*:clickhouse/*.xml";

    @Autowired
    private ClickHouseProperties clickHouseProperties;

    @Bean(name = "mclickhouseDataSource")
    public DataSource mclickhouseDataSource() throws SQLException {
        DruidDataSource datasource = new DruidDataSource();
        datasource.setUrl(clickHouseProperties.getUrl());
        datasource.setUsername(clickHouseProperties.getUsername());
        datasource.setPassword(clickHouseProperties.getPassword());
        datasource.setDriverClassName(clickHouseProperties.getDriverClassName());
        return datasource;
    }

    @Bean(name = "mclickhouseTransactionManager")
    public DataSourceTransactionManager mclickhouseTransactionManager() throws SQLException {
        return new DataSourceTransactionManager(mclickhouseDataSource());
    }

    @Bean("mclickhouseSqlSessionFactory")
    public SqlSessionFactory mclickhouseSqlSessionFactory(@Qualifier("mclickhouseDataSource") DataSource dataSource
            , MybatisPlusProperties mybatisProperties) throws Exception{
        final MybatisSqlSessionFactoryBean sessionFactory = new MybatisSqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        MybatisConfiguration newMybatisConfiguration = new MybatisConfiguration();
        BeanUtils.copyProperties(mybatisProperties.getConfiguration() ,newMybatisConfiguration);
        newMybatisConfiguration.setLogImpl(mybatisProperties.getConfiguration().getLogImpl());
        sessionFactory.setConfiguration(newMybatisConfiguration);
        Resource[] resources = new PathMatchingResourcePatternResolver().getResources(MAPPER_LOCATION_CLICKHOUSE);
        sessionFactory.setMapperLocations(resources);
        return sessionFactory.getObject();
    }
}
