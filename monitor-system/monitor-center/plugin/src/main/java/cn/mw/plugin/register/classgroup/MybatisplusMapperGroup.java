package cn.mw.plugin.register.classgroup;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ClassUtils;

import java.util.Set;

/**
 * @author qiyao(1210)
 * @date 2022-03-14
 */
public class MybatisplusMapperGroup implements PluginClassGroup {
    @Override
    public String groupId() {
        return "mybatisplus_bean";
    }

    @Override
    public void initialize(ApplicationContext applicationContext) {

    }

    @Override
    public boolean filter(Class<?> aClass) {
        if (!aClass.isInterface()) {
            Set<Class<?>> allInterfacesForClassAsSet = ClassUtils.getAllInterfacesForClassAsSet(aClass);
            return allInterfacesForClassAsSet.contains(BaseMapper.class);
        } else {
            return BaseMapper.class.isAssignableFrom(aClass);
        }

    }
}
