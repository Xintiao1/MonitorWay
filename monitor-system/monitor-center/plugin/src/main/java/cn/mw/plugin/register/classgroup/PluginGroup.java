package cn.mw.plugin.register.classgroup;

import cn.mw.plugin.utils.AnnotationsUtils;
import org.pf4j.Extension;
import org.springframework.context.ApplicationContext;


/**
 * 插件 所有接口类组
 * @author qiyao(1210)
 * @date 2022-03-11
 */
public class PluginGroup implements PluginClassGroup {

    @Override
    public String groupId() {
        return "plugin_extension_class";
    }

    @Override
    public void initialize(ApplicationContext applicationContext) {

    }

    @Override
    public boolean filter(Class<?> aClass) {
        return AnnotationsUtils.haveAnnotations(aClass, false, Extension.class);
    }
}
