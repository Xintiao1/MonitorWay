package cn.mw.plugin.register.classgroup;

import org.springframework.context.ApplicationContext;
/**
 * 插件-类-分组
 * 插件中需要 使用spring 依赖自动配置的类的分组
 * 包含
 * 0. plugin 的class组
 * 1. spring bean组
 * 2. controller组
 * 3. mybatis mapper接口组
 * 4. 服务API 扩展点组
 * @author qiyao(1210)
 * @date 2022-03-10
 */
public interface PluginClassGroup {

    String groupId();

    /**
     * 初始化。每处理一个插件, 该方法调用一次。
     * @param applicationContext 当前插件信息
     */
    void initialize(ApplicationContext applicationContext);

    /**
     * 过滤类。
     * @param aClass 类
     * @return 返回true.说明符合该分组器。false不符合该分组器
     */
    boolean filter(Class<?> aClass);
}
