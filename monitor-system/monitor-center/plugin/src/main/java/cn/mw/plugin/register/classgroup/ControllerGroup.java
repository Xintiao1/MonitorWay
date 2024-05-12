package cn.mw.plugin.register.classgroup;

import cn.mw.plugin.utils.AnnotationsUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;



/**
 * @author qiyao(1210)
 * @date 2022-03-14
 */
public class ControllerGroup implements PluginClassGroup {
    @Override
    public String groupId() {
        return "controller";
    }

    @Override
    public void initialize(ApplicationContext applicationContext) {

    }


    @Override
    public boolean filter(Class<?> aClass) {
        return AnnotationsUtils.haveAnnotations(aClass, false,
            Controller.class, RestController.class);
    }
}
