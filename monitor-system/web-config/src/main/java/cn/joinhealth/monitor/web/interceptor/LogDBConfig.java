package cn.joinhealth.monitor.web.interceptor;

import cn.mw.monitor.common.constant.Constants;
import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

@Configuration
@MapperScan(basePackages  = LogDBConfig.PACKAGE , sqlSessionFactoryRef = "logSqlSessionFactory")
public class LogDBConfig {

    public static final String PACKAGE = "cn.mw.syslog.dao";
    private static final String MAPPER_LOCATION = "classpath:mapper/log/*.xml";
    private static final String MAPPER_LOCATION_ORACLE = "classpath:oracle/log/*.xml";
    @Value("${spring.datasource.driverClassName}")
    private String driver;
    @Value("${spring.datasource.username3}")
    private String userName;
    @Value("${spring.datasource.passwd3}")
    private String passwd;
    @Value("${spring.datasource.url3}")
    private String url;
    @Value("${spring.datasource.sqllisten}")
    private boolean sqlListen;

    @Value("${datasource.check}")
    private String check;

    @Value("${orldatabase.username3}")
    private String oracleUsername;

    @Value("${orldatabase.passwd3}")
    private String oraclePassword;

    @Value("${orldatabase.driverClassName}")
    private String oracleDriverClassName;

    @Value("${orldatabase.url3}")
    private String oracledbUrl;

    @Bean(name="logDataSource")
    public DataSource logDataSource() throws SQLException {
        DruidDataSource datasource = new DruidDataSource();


        if (check.equals(Constants.DATABASE_MYSQL)){
            datasource.setUrl(url);
            datasource.setUsername(userName);
            datasource.setPassword(passwd);
            datasource.setDriverClassName(driver);

        }else if (check.equals(Constants.DATABASE_ORACLE)){
            datasource.setUrl(this.oracledbUrl);
            datasource.setUsername(oracleUsername);
            datasource.setPassword(oraclePassword);
            datasource.setDriverClassName(oracleDriverClassName);

        }else{
            datasource.setUrl(url);
            datasource.setUsername(userName);
            datasource.setPassword(passwd);
            datasource.setDriverClassName(driver);
        }


        if (sqlListen){
            datasource.addFilters("stat");
            datasource.addFilters("wall");
        }
        return datasource;
    }

    @Bean(name = "logTransactionManager")
    public DataSourceTransactionManager logTransactionManager() throws SQLException {
        return new DataSourceTransactionManager(logDataSource());
    }

    @Bean(name = "logSqlSessionFactory")
    public SqlSessionFactory logSqlSessionFactory(@Qualifier("logDataSource") DataSource logDataSource, MybatisPlusProperties mybatisProperties)
            throws Exception {
        final MybatisSqlSessionFactoryBean sessionFactory = new MybatisSqlSessionFactoryBean();
        sessionFactory.setDataSource(logDataSource);
        if (check.equals(Constants.DATABASE_MYSQL)){
            sessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver()
                    .getResources(LogDBConfig.MAPPER_LOCATION));
        }else if (check.equals(Constants.DATABASE_ORACLE)){
            sessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver()
                    .getResources(LogDBConfig.MAPPER_LOCATION_ORACLE));
        }else {
            sessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver()
                    .getResources(LogDBConfig.MAPPER_LOCATION));
        }
        sessionFactory.getObject().getConfiguration().setMapUnderscoreToCamelCase(true);
        Properties properties = new Properties();
        properties.setProperty("LOGTBSNAME","MONITORLOG");
        sessionFactory.setConfiguration(mybatisProperties.getConfiguration());
        return sessionFactory.getObject();
    }

    @Bean(name = "logSqlSessionTemplate")
    public SqlSessionTemplate logSqlSessionTemplate(@Qualifier("logSqlSessionFactory") SqlSessionFactory logSqlSessionFactory) {
        return new SqlSessionTemplate(logSqlSessionFactory);
    }

}
