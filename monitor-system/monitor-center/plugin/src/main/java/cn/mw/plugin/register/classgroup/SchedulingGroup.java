package cn.mw.plugin.register.classgroup;

import cn.mw.plugin.utils.AnnotationsUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 检测
 * @author qiyao(1210)
 * @date 2022-03-19
 */

public class SchedulingGroup implements PluginClassGroup {
    @Override
    public String groupId() {
        return null;
    }

    @Override
    public void initialize(ApplicationContext applicationContext) {

    }

    @Override
    public boolean filter(Class<?> aClass) {
        return AnnotationsUtils.haveAnnotations(aClass, false, EnableScheduling.class);
    }
}
