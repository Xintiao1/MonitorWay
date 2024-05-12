package cn.joinhealth.monitor.web.interceptor;

import cn.mw.MybatisInterceptor;
import cn.mw.monitor.common.constant.Constants;
import cn.mw.monitor.util.RSAUtils;
import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @author xhy
 * @date 2020/4/25 17:28
 */
@Configuration
@MapperScan(basePackages = MasterDbConfig.PACKAGE, sqlSessionFactoryRef = "masterSqlSessionFactory")
@Slf4j
@Import({MybatisInterceptor.class})
public class MasterDbConfig {

    private Logger logger = LoggerFactory.getLogger(MasterDbConfig.class);
    // 精确到 master 目录，以便跟其他数据源隔离
    static final String PACKAGE = "cn.mw.monitor.**.dao";
    private static final String MAPPER_LOCATION = "classpath*:mapper/*.xml";
    private static final String MAPPER_LOCATION_ORACLE = "classpath*:oracle/*.xml";
    private static final String ZABBIX_SERVER_LOCATION_ORACLE = "classpath*:oracle/*/*.xml";
    private static final String ZABBIX_SERVER_LOCATION = "classpath*:mapper/*/*.xml";
    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${datasource.check}")
    private String check;


    @Autowired
    private MybatisInterceptor mybatisInterceptor;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driverClassName}")
    private String driverClassName;

    @Value("${orldatabase.username}")
    private String oracleUsername;

    @Value("${orldatabase.password}")
    private String oraclePassword;

    @Value("${orldatabase.driverClassName}")
    private String oracleDriverClassName;

    @Value("${orldatabase.url}")
    private String oracledbUrl;

    @Value("${spring.datasource.sqllisten}")
    private boolean sqlListen;

    /**
     * 是否加密
     */
    @Value("${spring.datasource.isencrypt}")
    private boolean isEncrypt;

    @Bean(name = "masterDataSource")   //声明其为Bean实例
    @Primary  //在同样的DataSource中，首先使用被标注的DataSource
    public DataSource masterDataSource() throws SQLException {
        DruidDataSource datasource = new DruidDataSource();
        //password解密
        String dbPassword = password;
        if (isEncrypt) {
            dbPassword = RSAUtils.decryptData(password, RSAUtils.RSA_PRIVATE_KEY);
        }
        if (check.equals(Constants.DATABASE_MYSQL)){
            datasource.setUrl(this.dbUrl);
            datasource.setUsername(username);
            datasource.setPassword(dbPassword);
            datasource.setDriverClassName(driverClassName);

        }else if (check.equals(Constants.DATABASE_ORACLE)){
            datasource.setUrl(this.oracledbUrl);
            datasource.setUsername(oracleUsername);
            datasource.setPassword(oraclePassword);
            datasource.setDriverClassName(oracleDriverClassName);

        }else{
            datasource.setUrl(this.dbUrl);
            datasource.setUsername(username);
            datasource.setPassword(dbPassword);
            datasource.setDriverClassName(driverClassName);
        }


        if (sqlListen){
            datasource.addFilters("stat");
            datasource.addFilters("wall");
        }
        return datasource;
    }

    @Bean(name = "masterTransactionManager")
    @Primary
    public DataSourceTransactionManager masterTransactionManager() throws SQLException {
        return new DataSourceTransactionManager(masterDataSource());
    }

    @Bean(name = "masterSqlSessionFactory")
    @Primary
    public SqlSessionFactory masterSqlSessionFactory(@Qualifier("masterDataSource") DataSource masterDataSource, MybatisPlusProperties mybatisProperties)
            throws Exception {
        final MybatisSqlSessionFactoryBean sessionFactory = new MybatisSqlSessionFactoryBean();
        sessionFactory.setDataSource(masterDataSource);
        ArrayList<Resource> list = new ArrayList<>();
        List<Resource> list1 = new ArrayList<>();

        List<Resource> list2 = new ArrayList<>();
        if (check.equals(Constants.DATABASE_MYSQL)){
            list2 = Arrays.asList(new PathMatchingResourcePatternResolver().getResources( MasterDbConfig.MAPPER_LOCATION));
            Resource[] resources = new PathMatchingResourcePatternResolver().getResources(MasterDbConfig.ZABBIX_SERVER_LOCATION);
            list1=Arrays.asList(resources);
        }else if (check.equals(Constants.DATABASE_ORACLE)){
            list2 = Arrays.asList(new PathMatchingResourcePatternResolver().getResources( MasterDbConfig.MAPPER_LOCATION_ORACLE));
            Resource[] resources = new PathMatchingResourcePatternResolver().getResources(MasterDbConfig.ZABBIX_SERVER_LOCATION_ORACLE);
            list1=Arrays.asList(resources);
            sessionFactory.setPlugins(new Interceptor[]{mybatisInterceptor});
        }else{
            list2 = Arrays.asList(new PathMatchingResourcePatternResolver().getResources( MasterDbConfig.MAPPER_LOCATION));
            Resource[] resources = new PathMatchingResourcePatternResolver().getResources(MasterDbConfig.ZABBIX_SERVER_LOCATION);
            list1=Arrays.asList(resources);
        }

        list.addAll(list1);
        list.addAll(list2);
        Resource[] resource = new Resource[list.size()];
        for (int i = 0; i < resource.length; i++) {
            resource[i] = list.get(i);
        }
                sessionFactory.setMapperLocations(resource);
//        sessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver()
//                .getResources(MasterDbConfig.MAPPER_LOCATION));
        MybatisConfiguration mybatisConfiguration =mybatisProperties.getConfiguration();
        mybatisConfiguration.setLogImpl(mybatisProperties.getConfiguration().getLogImpl());
        if (check.equals(Constants.DATABASE_ORACLE)){
            mybatisConfiguration.setJdbcTypeForNull(JdbcType.NULL);
        }
        sessionFactory.setConfiguration(mybatisConfiguration);
        //mybatis 数据库字段与实体类属性驼峰映射配置
        sessionFactory.getObject().getConfiguration().setMapUnderscoreToCamelCase(true);
        return sessionFactory.getObject();
    }
}
