package cn.mw.module.solarwind;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/**
 * @author xhy
 * @date 00/6/ 14:7
 */

@Configuration
@EnableAutoConfiguration
@ConditionalOnProperty(prefix = "solarwind", name = "enable", havingValue = "true")
@PropertySource( value="classpath:solarwind.properties")
@MapperScan(basePackages = SolarwindDbConfig.PACKAGE, sqlSessionFactoryRef = "solarwindSqlSessionTemplate")
@Slf4j
public class SolarwindDbConfig {
    private Logger logger = LoggerFactory.getLogger(SolarwindDbConfig.class);
    // 精确到 sqlserver 目录，以便跟其他数据源隔离
    static final String PACKAGE = "cn.mw.module.solarwind.dao";
    private static final String MAPPER_LOCATION = "classpath*:mapper.solarmapper/*.xml";

    @Value("${solarwind.datasource.url}")
    private String dbUrl;

    @Value("${solarwind.datasource.username}")
    private String username;

    @Value("${solarwind.datasource.password}")
    private String password;

    @Value("${solarwind.datasource.driverClassName}")
    private String driverClassName;


 /*   @Bean(name = "sqlServerDataSource")   //声明其为Bean实例
    public DataSource sqlServerDataSource() {
        DruidDataSource datasource = new DruidDataSource();

        datasource.setUrl(this.dbUrl);
        datasource.setUsername(username);
        datasource.setPassword(password);
        datasource.setDriverClassName(driverClassName);
        return datasource;
    }

    @Bean(name = "sqlServerTransactionManager")
    public DataSourceTransactionManager sqlServerTransactionManager() {
        return new DataSourceTransactionManager(sqlServerDataSource());
    }

    @Bean(name = "sqlServerSessionFactory")
    public SqlSessionFactory sqlServerSessionFactory(@Qualifier("sqlServerDataSource") DataSource sqlServerDataSource)
            throws Exception {
        final SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(sqlServerDataSource);
        sessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources(SqlServerDbConfig.MAPPER_LOCATION));
        sessionFactory.getObject().getConfiguration().setMapUnderscoreToCamelCase(true);
        return sessionFactory.getObject();
    }*/

    @Bean(name = "solarwindDataSource")   //声明其为Bean实例
    @ConfigurationProperties(prefix="solarwind.datasource")
    public DataSource solarwindDataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    @Bean(name = "solarwindTransactionManager")
    public DataSourceTransactionManager solarwindTransactionManager() {
        return new DataSourceTransactionManager(solarwindDataSource());
    }

    @Bean(name = "solarwindSqlSessionTemplate")
    public SqlSessionFactory solarwindSqlSessionFactory(@Qualifier("solarwindDataSource") DataSource solarwindDataSource)
            throws Exception {
        final SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(solarwindDataSource);
        PathMatchingResourcePatternResolver pmrp = new PathMatchingResourcePatternResolver();
        Resource[] resources = pmrp.getResources(SolarwindDbConfig.MAPPER_LOCATION);
        sessionFactory.setMapperLocations(resources);
        //mybatis 数据库字段与实体类属性驼峰映射配置
        sessionFactory.getObject().getConfiguration().setMapUnderscoreToCamelCase(true);
        return sessionFactory.getObject();
    }

}
