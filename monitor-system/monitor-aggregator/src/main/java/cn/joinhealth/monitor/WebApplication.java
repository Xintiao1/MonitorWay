package cn.joinhealth.monitor;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


/**
 * Created by yeshengqi on 2019/4/9.
 */
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class})
@ComponentScan(basePackages = {
        "cn.joinhealth.monitor",
        "cn.joinhealth.zbx",
        "cn.joinhealth.echarts",
        "cn.mw.config",
        "cn.mw.zbx",
        "cn.mw.monitor",
        "cn.mw.syslog",
        "cn.mw.time",
        "cn.mw.module.solarwind",
        "cn.mw.module.security",
        "cn.mw.monitor.license.aop",
        "cn.mw.monitor.license.util"

})
@MapperScan({
        "cn.joinhealth.monitor.user.dao",
        "cn.joinhealth.monitor.menu.dao",
        "cn.joinhealth.monitor.role.dao",
        "cn.joinhealth.monitor.org.dao",
        "cn.joinhealth.monitor.dept.dao",
        "cn.joinhealth.monitor.know.dao",
        "cn.joinhealth.monitor.zbx.dao",
        "cn.joinhealth.monitor.license.dao",
        "cn.joinhealth.monitor.ip.dao",
        "cn.joinhealth.monitor.assets.dao",
        "cn.joinhealth.monitor.test.dao",
        "cn.joinhealth.monitor.screen.dao"
})
@EnableSwagger2
@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
public class WebApplication {
    ClassPathResource d;
    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }

}
