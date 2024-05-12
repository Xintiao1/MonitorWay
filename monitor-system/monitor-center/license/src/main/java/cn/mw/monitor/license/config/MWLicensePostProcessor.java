package cn.mw.monitor.license.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Properties;

public class MWLicensePostProcessor implements EnvironmentPostProcessor {

    private final Properties licenseProperties = new Properties();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String customEnv = environment.getProperty("CUSTOM-ENV");
        //先从默认的路径找到
        Resource resource = new ClassPathResource("license.properties");
        environment.getPropertySources().addLast(loadProfiles(resource));
        //如果外挂有数据 优先从外挂覆盖
        if(null != customEnv && !"".equals(customEnv.trim())){
            Resource resource1 = new PathResource(customEnv);
            environment.getPropertySources().addFirst(loadProfiles(resource1));
        }



    }

    private PropertySource<?> loadProfiles(Resource resource) {
        if (!resource.exists()) {
            throw new IllegalArgumentException("file" + resource + "not exist");
        }
        try {
            licenseProperties.load(resource.getInputStream());
            return new PropertiesPropertySource(resource.getFilename(), licenseProperties);
        } catch (IOException ex) {
            throw new IllegalStateException("load resource exception" + resource, ex);
        }
    }

}
