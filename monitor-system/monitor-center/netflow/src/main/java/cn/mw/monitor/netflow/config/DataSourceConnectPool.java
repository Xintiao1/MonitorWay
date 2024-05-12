package cn.mw.monitor.netflow.config;


import cn.mw.monitor.netflow.service.MwNetflowDetailService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * @author guiquanwnag
 * @datetime 2023/7/19
 * @Description 数据源连接池
 */
@Configuration
public class DataSourceConnectPool {

    @Value("${storage.clickhouse.url}")
    private String url;
    @Value("${storage.clickhouse.username}")
    private String username;
    @Value("${storage.clickhouse.password}")
    private String password;
    @Value("${cap.storage.type}")
    private int storageType;


    @Bean
    public DataSource clickhouseDataSource() {
        if (MwNetflowDetailService.STORAGE_CLICKHOUSE == storageType) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(url);
            config.setUsername(username);
            config.setPassword(password);
            return new HikariDataSource(config);
        }
        return null;
    }

}
