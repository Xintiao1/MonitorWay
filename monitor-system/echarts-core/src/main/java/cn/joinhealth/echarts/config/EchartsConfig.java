package cn.joinhealth.echarts.config;


import cn.joinhealth.echarts.echart.DefaultEchartsFactory;
import cn.joinhealth.echarts.echart.EchartsFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EchartsConfig {
    @Bean
    public EchartsFactory getEchartsFactory() {
        return DefaultEchartsFactory.getEchartsFactory();
    }
}
