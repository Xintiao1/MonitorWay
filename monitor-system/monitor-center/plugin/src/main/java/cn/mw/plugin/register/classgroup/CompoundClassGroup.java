package cn.mw.plugin.register.classgroup;

import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 * @author qiyao(1210)
 * @date 2022-03-11
 */
public abstract class CompoundClassGroup implements PluginClassGroup {
    protected List<PluginClassGroup> groupList = Collections.synchronizedList(new ArrayList<>());

    /**
     * 初始化。每处理一个插件, 该方法调用一次.
     * @param applicationContext 当前插件信息
     */
    @Override
    public void initialize(ApplicationContext applicationContext) {
        groupList.forEach(t -> {
            t.initialize(applicationContext);
        });
    }

    /**
     * 过滤类.
     * @param aClass 类
     * @return 返回true.说明符合该分组器。false不符合该分组器
     */
    @Override
    public boolean filter(Class<?> aClass) {
        for (PluginClassGroup group : groupList) {
            //现在只支持或的关系
            if (group.filter(aClass)) {
                return true;
            }
        }
        return false;
    }
}
