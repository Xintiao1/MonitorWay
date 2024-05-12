package cn.mw.monitor.visualized.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Properties;

/**
 * @ClassName MWVisualizedProcessor
 * @Description ToDo
 * @Author gengjb
 * @Date 2022/4/15 16:57
 * @Version 1.0
 **/
public class MWVisualizedProcessor implements EnvironmentPostProcessor {
    private final Properties customProperties = new Properties();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Resource resource = new ClassPathResource("visualized.properties");
        environment.getPropertySources().addLast(loadProfiles(resource));
    }

    private PropertySource<?> loadProfiles(Resource resource) {
        if (!resource.exists()) {
            throw new IllegalArgumentException("file" + resource + "not exist");
        }
        try {
            customProperties.load(resource.getInputStream());
            return new PropertiesPropertySource(resource.getFilename(), customProperties);
        } catch (IOException ex) {
            throw new IllegalStateException("load resource exception" + resource, ex);
        }
    }
}
