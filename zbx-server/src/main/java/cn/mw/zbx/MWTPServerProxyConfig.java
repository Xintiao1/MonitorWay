package cn.mw.zbx;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class MWTPServerProxyConfig {

    @Value("${monitor.zabbix.debug}")
    private boolean debug;

    @Bean
    public MWTPServerProxyFactoryBean getPoxyFactoryBean() {
        log.info("getPoxyFactoryBean debug is " + debug);
        MWTPServerProxyFactoryBean mwtpServerProxyFactoryBean = new MWTPServerProxyFactoryBean();
        mwtpServerProxyFactoryBean.setDebug(debug);
        return mwtpServerProxyFactoryBean;
    }

}
