package cn.mw.plugin.register.classgroup;

import cn.mw.plugin.utils.AnnotationsUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

/**
 * Spring组件组.
 * @author qiyao(1210)
 * @date 2022-03-11
 */
public class SpringComponentGroup implements PluginClassGroup {
    @Override
    public String groupId() {
        return "spring_component";
    }

    @Override
    public void initialize(ApplicationContext applicationContext) {

    }

    @Override
    public boolean filter(Class<?> aClass) {
        return !aClass.isAnnotation() && !aClass.isInterface() && AnnotationsUtils.haveAnnotations(aClass, false,
            Component.class, Service.class,
            Controller.class, RestController.class, Configuration.class);
    }
}
