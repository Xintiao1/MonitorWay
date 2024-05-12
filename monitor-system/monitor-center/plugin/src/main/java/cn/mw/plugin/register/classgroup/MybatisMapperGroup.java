package cn.mw.plugin.register.classgroup;

import cn.mw.plugin.utils.AnnotationsUtils;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.context.ApplicationContext;


/**
 * @author qiyao(1210)
 * @date
 */
public class MybatisMapperGroup implements PluginClassGroup {
    @Override
    public String groupId() {
        return "mybatis_bean";
    }

    @Override
    public void initialize(ApplicationContext applicationContext) {

    }


    @Override
    public boolean filter(Class<?> aClass) {
        return AnnotationsUtils.haveAnnotations(aClass, false, Mapper.class);
    }
}
